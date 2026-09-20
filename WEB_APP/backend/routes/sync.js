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
      { uid, date },
      {
        totalScreenTimeMs,
        distractiveTimeMs,
        distractionFreeTimeMs,
        appUsage,
        updatedAt: new Date()
      },
      { upsert: true, new: true }
    );

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
