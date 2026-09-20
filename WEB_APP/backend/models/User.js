const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  uid: { type: String, required: true, unique: true }, // Firebase UID
  email: String,
  phone: String,
  createdAt: { type: Date, default: Date.now },
  currentTreeStage: { type: String, default: 'Seed' },
  healthRatio: { type: Number, default: 1.0 },
});

module.exports = mongoose.model('User', userSchema);
