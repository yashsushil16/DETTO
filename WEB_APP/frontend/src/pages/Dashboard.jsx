import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { motion, AnimatePresence } from 'framer-motion';
import { LogOut, Activity, Clock, ShieldCheck, TreePine } from 'lucide-react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer
} from 'recharts';

export default function Dashboard() {
  const { currentUser, logout } = useAuth();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchData() {
      try {
        const BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5000';
        const res = await fetch(`${BASE}/api/sync/data/${currentUser.uid}`);
        if (res.ok) {
          const json = await res.json();
          setData(json);
        }
      } catch (e) {
        console.error("Failed to fetch analytics:", e);
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, [currentUser]);

  if (loading) {
    return <div className="min-h-screen flex items-center justify-center bg-[#0a0a0a] text-white">Loading...</div>;
  }

  const userDoc = data?.user || {};
  const analyticsList = data?.analytics || [];
  
  // Prepare chart data (reverse for chronological order)
  const chartData = [...analyticsList].reverse().map(item => ({
    date: item.date.substring(5), // MM-DD
    focus: item.distractionFreeTimeMs / (1000 * 60 * 60), // Hours
    distraction: item.distractiveTimeMs / (1000 * 60 * 60)
  }));

  const today = analyticsList[0] || {};
  const todayFocusHours = ((today.distractionFreeTimeMs || 0) / (1000 * 60 * 60)).toFixed(1);
  const todayScreenHours = ((today.totalScreenTimeMs || 0) / (1000 * 60 * 60)).toFixed(1);

  return (
    <div className="min-h-screen bg-[#050505] text-white selection:bg-green-500/30 font-sans selection:text-white">
      {/* Top Navigation */}
      <nav className="border-b border-white/5 bg-black/50 backdrop-blur-md sticky top-0 z-50">
        <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <TreePine className="w-5 h-5 text-green-500" />
            <span className="font-bold tracking-widest">DETTO CLOUD</span>
          </div>
          <div className="flex items-center space-x-6">
            <span className="text-sm text-gray-400">{currentUser.email}</span>
            <button onClick={logout} className="text-gray-400 hover:text-white transition-colors">
              <LogOut className="w-5 h-5" />
            </button>
          </div>
        </div>
      </nav>

      <main className="max-w-6xl mx-auto px-6 py-12">
        {/* Hero Section: Zen Tree */}
        <section className="mb-16 relative">
          <div className="absolute inset-0 bg-green-500/5 blur-[150px] rounded-full pointer-events-none" />
          <motion.div 
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="bg-[#111] border border-white/5 rounded-3xl p-10 flex flex-col md:flex-row items-center justify-between overflow-hidden relative"
          >
            <div className="z-10 flex-1 mb-8 md:mb-0">
              <h1 className="text-4xl md:text-5xl font-bold mb-4 tracking-tight">Your Digital Garden</h1>
              <p className="text-gray-400 text-lg mb-8 max-w-md">
                Syncing your progress from the DETTO Android app. Watch your focus bloom over time.
              </p>
              <div className="inline-flex items-center space-x-3 bg-white/5 px-4 py-2 rounded-full border border-white/10">
                <span className="w-2 h-2 rounded-full bg-green-500 animate-pulse" />
                <span className="text-sm font-medium">Status: {userDoc.currentTreeStage || 'Seed'}</span>
              </div>
            </div>

            {/* Tree Animation placeholder. Real implementation can use complex SVGs or Rive/Lottie */}
            <div className="z-10 w-64 h-64 relative flex items-center justify-center bg-black/20 rounded-full border border-white/5 shadow-2xl">
              <motion.div
                animate={{ y: [0, -10, 0] }}
                transition={{ repeat: Infinity, duration: 4, ease: "easeInOut" }}
                className="text-8xl"
              >
                {userDoc.currentTreeStage === 'Sprout' ? '✿' :
                 userDoc.currentTreeStage === 'Plant' ? '✾' :
                 userDoc.currentTreeStage === 'Tree' ? '↟' :
                 userDoc.currentTreeStage === 'Garden' ? '✤' :
                 userDoc.currentTreeStage === 'Forest' ? '❁' : '☘'}
              </motion.div>
            </div>
          </motion.div>
        </section>

        {/* Quick Stats */}
        <section className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-16">
          <StatCard 
            icon={<ShieldCheck />} 
            label="Today's Focus Time" 
            value={`${todayFocusHours}h`} 
            delay={0.1} 
          />
          <StatCard 
            icon={<Activity />} 
            label="Total Screen Time" 
            value={`${todayScreenHours}h`} 
            delay={0.2} 
          />
          <StatCard 
            icon={<Clock />} 
            label="Current Stage" 
            value={userDoc.currentTreeStage || 'Seed'} 
            delay={0.3} 
          />
        </section>

        {/* Analytics Chart */}
        <section>
          <div className="flex items-center justify-between mb-8">
            <h2 className="text-2xl font-bold">Focus Trends (Last 30 Days)</h2>
          </div>
          
          <motion.div 
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            viewport={{ once: true }}
            className="bg-[#111] border border-white/5 rounded-3xl p-6 h-[400px]"
          >
            {chartData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <defs>
                    <linearGradient id="colorFocus" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#22c55e" stopOpacity={0.3}/>
                      <stop offset="95%" stopColor="#22c55e" stopOpacity={0}/>
                    </linearGradient>
                    <linearGradient id="colorDist" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#ef4444" stopOpacity={0.3}/>
                      <stop offset="95%" stopColor="#ef4444" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <XAxis dataKey="date" stroke="#333" tick={{fill: '#666', fontSize: 12}} tickLine={false} axisLine={false} />
                  <YAxis stroke="#333" tick={{fill: '#666', fontSize: 12}} tickLine={false} axisLine={false} />
                  <CartesianGrid strokeDasharray="3 3" stroke="#222" vertical={false} />
                  <Tooltip 
                    contentStyle={{ backgroundColor: '#1a1a1a', border: '1px solid #333', borderRadius: '8px' }}
                    itemStyle={{ color: '#fff' }}
                  />
                  <Area type="monotone" dataKey="focus" stroke="#22c55e" fillOpacity={1} fill="url(#colorFocus)" name="Focus (h)" />
                  <Area type="monotone" dataKey="distraction" stroke="#ef4444" fillOpacity={1} fill="url(#colorDist)" name="Distracted (h)" />
                </AreaChart>
              </ResponsiveContainer>
            ) : (
              <div className="w-full h-full flex items-center justify-center text-gray-500">
                No data available yet. Sync your app!
              </div>
            )}
          </motion.div>
        </section>

      </main>
    </div>
  );
}

function StatCard({ icon, label, value, delay }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay, duration: 0.5 }}
      className="bg-[#111] border border-white/5 p-6 rounded-2xl flex flex-col justify-between"
    >
      <div className="flex items-center space-x-3 mb-4">
        <div className="p-2 bg-white/5 rounded-lg text-gray-400">
          {icon}
        </div>
        <span className="text-sm font-medium text-gray-400">{label}</span>
      </div>
      <div className="text-3xl font-bold">{value}</div>
    </motion.div>
  );
}
