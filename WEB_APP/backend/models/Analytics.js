const mongoose = require('mongoose');

const analyticsSchema = new mongoose.Schema({
  uid: { type: String, required: true }, // Links to Firebase UID
  date: { type: String, required: true }, // YYYY-MM-DD
  totalScreenTimeMs: { type: Number, default: 0 },
  distractiveTimeMs: { type: Number, default: 0 },
  distractionFreeTimeMs: { type: Number, default: 0 },
  appUsage: [
    {
      packageName: String,
      appName: String,
      usageMs: Number,
      limitMs: Number
    }
  ],
  updatedAt: { type: Date, default: Date.now }
});

// Ensure only one analytics record per user per day
analyticsSchema.index({ uid: 1, date: 1 }, { unique: true });

module.exports = mongoose.model('Analytics', analyticsSchema);
