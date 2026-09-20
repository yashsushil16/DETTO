const express = require('express');
const router = express.Router();
const User = require('../models/User');
const Analytics = require('../models/Analytics');

// The Android app hits this endpoint every 15 minutes
router.post('/push', async (req, res) => {
  try {
    const { 
      uid, 
      email, 
      phone, 
      date, 
      totalScreenTimeMs, 
      distractiveTimeMs, 
      distractionFreeTimeMs, 
      appUsage,
      treeStage,
      healthRatio
    } = req.body;

    if (!uid || !date) {
      return res.status(400).json({ error: 'Missing required fields' });
    }

    // Upsert User
    await User.findOneAndUpdate(
      { uid },
      { 
        email, 
        phone, 
        currentTreeStage: treeStage, 
        healthRatio 
      },
      { upsert: true, new: true }
    );

    // Upsert Analytics for today
    await Analytics.findOneAndUpdate(
      { uid, date, recordType: 'daily' },
      {
        totalScreenTimeMs,
        distractiveTimeMs,
        distractionFreeTimeMs,
        appUsage,
        updatedAt: new Date()
      },
      { upsert: true, new: true }
    );

    // Auto-Archiving logic to save MongoDB space
    try {
      const dailyRecords = await Analytics.find({ uid, recordType: 'daily' }).sort({ date: 1 });
      if (dailyRecords.length > 14) {
        const recordsToArchive = dailyRecords.slice(0, dailyRecords.length - 14);
        const weeklyGroups = {};
        
        recordsToArchive.forEach(record => {
          const d = new Date(record.date);
          d.setDate(d.getDate() - d.getDay()); // Start of week (Sunday)
          const weekKey = `${d.getFullYear()}-W${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
          
          if (!weeklyGroups[weekKey]) {
            weeklyGroups[weekKey] = {
              totalScreenTimeMs: 0,
              distractiveTimeMs: 0,
              distractionFreeTimeMs: 0,
              appUsageMap: {},
              recordIds: []
            };
          }
          
          const group = weeklyGroups[weekKey];
          group.totalScreenTimeMs += record.totalScreenTimeMs || 0;
          group.distractiveTimeMs += record.distractiveTimeMs || 0;
          group.distractionFreeTimeMs += record.distractionFreeTimeMs || 0;
          group.recordIds.push(record._id);
          
          if (record.appUsage) {
            record.appUsage.forEach(app => {
              if (!group.appUsageMap[app.packageName]) {
                group.appUsageMap[app.packageName] = { 
                  packageName: app.packageName, 
                  appName: app.appName, 
                  limitMs: app.limitMs, 
                  usageMs: 0 
                };
              }
              group.appUsageMap[app.packageName].usageMs += app.usageMs || 0;
            });
          }
        });
        
        for (const [weekKey, group] of Object.entries(weeklyGroups)) {
          let weeklyRecord = await Analytics.findOne({ uid, date: weekKey, recordType: 'weekly' });
          if (!weeklyRecord) {
            weeklyRecord = new Analytics({ uid, date: weekKey, recordType: 'weekly' });
          }
          
          weeklyRecord.totalScreenTimeMs += group.totalScreenTimeMs;
          weeklyRecord.distractiveTimeMs += group.distractiveTimeMs;
          weeklyRecord.distractionFreeTimeMs += group.distractionFreeTimeMs;
          
          const existingApps = weeklyRecord.appUsage || [];
          const existingAppMap = {};
          existingApps.forEach(a => { existingAppMap[a.packageName] = a; });
          
          for (const newApp of Object.values(group.appUsageMap)) {
            if (existingAppMap[newApp.packageName]) {
              existingAppMap[newApp.packageName].usageMs += newApp.usageMs;
            } else {
              existingApps.push(newApp);
              existingAppMap[newApp.packageName] = newApp;
            }
          }
          
          weeklyRecord.appUsage = existingApps;
          weeklyRecord.updatedAt = new Date();
          await weeklyRecord.save();
        }
        
        const idsToDelete = recordsToArchive.map(r => r._id);
        await Analytics.deleteMany({ _id: { $in: idsToDelete } });
      }
    } catch (archiveErr) {
      console.error('Archiving error:', archiveErr);
    }

    res.status(200).json({ success: true, message: 'Data synced successfully' });
  } catch (error) {
    console.error('Sync error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// For the Web Dashboard to fetch analytics
router.get('/data/:uid', async (req, res) => {
  try {
    const { uid } = req.params;
    const user = await User.findOne({ uid });
    const analytics = await Analytics.find({ uid }).sort({ date: -1 }).limit(30);
    
    if (!user) return res.status(404).json({ error: 'User not found' });

    res.status(200).json({ user, analytics });
  } catch (error) {
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
