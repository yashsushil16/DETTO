import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { motion } from 'framer-motion';
import { LogOut, Activity, Clock, ShieldCheck, Cloud } from 'lucide-react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer
} from 'recharts';
import TreeVisualizer from '../components/TreeVisualizer';

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
    return <div className="min-h-screen flex items-center justify-center bg-[#050505] text-[#E0E0E0] font-sans">Loading...</div>;
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

  const currentStage = userDoc.currentTreeStage || 'Seed';
  const healthRatio = userDoc.healthRatio || 1.0;

  return (
    <div className="min-h-screen bg-[#050505] text-[#E0E0E0] selection:bg-white/20 font-sans selection:text-white">
      {/* Top Navigation */}
      <nav className="border-b border-[#222] bg-[#0A0A0A] sticky top-0 z-50">
        <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <Cloud className="w-5 h-5 text-[#888]" />
            <span className="font-bold tracking-[0.2em] text-sm uppercase">Detto</span>
          </div>
          <div className="flex items-center space-x-6">
            <span className="text-xs text-[#888]">{currentUser.email}</span>
            <button onClick={logout} className="text-[#888] hover:text-white transition-colors">
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </nav>

      <main className="max-w-6xl mx-auto px-6 py-12">
        {/* Hero Section: Zen Tree */}
        <section className="mb-12">
          <motion.div 
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            className="bg-[#0A0A0A] border border-[#222] rounded-2xl p-10 flex flex-col md:flex-row items-center justify-between relative overflow-hidden"
          >
            <div className="z-10 flex-1 mb-10 md:mb-0">
              <h1 className="text-3xl font-semibold mb-2 tracking-tight text-white">Digital Garden</h1>
              <p className="text-[#888] text-sm mb-8 max-w-md">
                Syncing progress from your Android device. 
              </p>
              
              <div className="flex flex-col space-y-2">
                <div className="text-xs text-[#666] uppercase tracking-widest">Current Stage</div>
                <div className="text-xl font-medium text-white">{currentStage}</div>
              </div>
            </div>

            {/* Tree Visualizer */}
            <div className="z-10 w-[300px] h-[300px] md:w-[400px] md:h-[400px] relative flex items-center justify-center bg-[#050505] rounded-full border border-[#111] shadow-2xl overflow-hidden">
               <TreeVisualizer stage={currentStage} healthRatio={healthRatio} showGlow={true} />
            </div>
          </motion.div>
        </section>

        {/* Quick Stats */}
        <section className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
          <StatCard 
            icon={<ShieldCheck className="w-4 h-4" />} 
            label="Today's Focus" 
            value={`${todayFocusHours}h`} 
            delay={0.1} 
          />
          <StatCard 
            icon={<Activity className="w-4 h-4" />} 
            label="Total Screen Time" 
            value={`${todayScreenHours}h`} 
            delay={0.2} 
          />
          <StatCard 
            icon={<Clock className="w-4 h-4" />} 
            label="Current Stage" 
            value={currentStage} 
            delay={0.3} 
          />
        </section>

        {/* Analytics Chart */}
        <section>
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-lg font-medium text-white">Focus Trends</h2>
          </div>
          
          <motion.div 
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            viewport={{ once: true }}
            className="bg-[#0A0A0A] border border-[#222] rounded-2xl p-6 h-[350px]"
          >
            {chartData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <defs>
                    <linearGradient id="colorFocus" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#FFFFFF" stopOpacity={0.15}/>
                      <stop offset="95%" stopColor="#FFFFFF" stopOpacity={0}/>
                    </linearGradient>
                    <linearGradient id="colorDist" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#666666" stopOpacity={0.15}/>
                      <stop offset="95%" stopColor="#666666" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <XAxis dataKey="date" stroke="#444" tick={{fill: '#888', fontSize: 10}} tickLine={false} axisLine={false} />
                  <YAxis stroke="#444" tick={{fill: '#888', fontSize: 10}} tickLine={false} axisLine={false} />
                  <CartesianGrid strokeDasharray="3 3" stroke="#1A1A1A" vertical={false} />
                  <Tooltip 
                    contentStyle={{ backgroundColor: '#111', border: '1px solid #333', borderRadius: '4px', fontSize: '12px' }}
                    itemStyle={{ color: '#fff' }}
                  />
                  <Area type="monotone" dataKey="focus" stroke="#FFFFFF" strokeWidth={2} fillOpacity={1} fill="url(#colorFocus)" name="Focus (h)" />
                  <Area type="monotone" dataKey="distraction" stroke="#666666" strokeWidth={2} fillOpacity={1} fill="url(#colorDist)" name="Distracted (h)" />
                </AreaChart>
              </ResponsiveContainer>
            ) : (
              <div className="w-full h-full flex items-center justify-center text-[#666] text-sm">
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
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay, duration: 0.5 }}
      className="bg-[#0A0A0A] border border-[#222] p-6 rounded-2xl flex flex-col justify-between"
    >
      <div className="flex items-center space-x-3 mb-4">
        <div className="p-2 bg-[#111] rounded text-[#888]">
          {icon}
        </div>
        <span className="text-xs font-medium text-[#888] uppercase tracking-widest">{label}</span>
      </div>
      <div className="text-2xl font-semibold text-white">{value}</div>
    </motion.div>
  );
}
