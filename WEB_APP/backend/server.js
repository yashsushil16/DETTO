require('dotenv').config();
const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');

const syncRoutes = require('./routes/sync');

const app = express();
app.use(cors());
app.use(express.json());

app.use('/api/sync', syncRoutes);

const PORT = process.env.PORT || 5000;

mongoose.connect(process.env.MONGO_URI, { useNewUrlParser: true, useUnifiedTopology: true })
  .then(() => {
    console.log('Connected to MongoDB Atlas');
    app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
  })
  .catch((err) => console.error('MongoDB connection error:', err));
