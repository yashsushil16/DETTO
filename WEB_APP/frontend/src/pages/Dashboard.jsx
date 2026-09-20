import React, { useEffect, useState, useMemo } from 'react';
import { useAuth } from '../context/AuthContext';
import { motion, AnimatePresence } from 'framer-motion';
import { LogOut, Activity, Clock, ShieldCheck, Leaf, Target, BarChart2 } from 'lucide-react';
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
  const [timeRange, setTimeRange] = useState('Daily');

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

  const userDoc = data?.user || {};
  const analyticsList = data?.analytics || [];

  // Aggregation Logic
  const chartData = useMemo(() => {
    if (!analyticsList.length) return [];
    
    // Sort chronological for charts
    const sorted = [...analyticsList].sort((a, b) => new Date(a.date) - new Date(b.date));

    if (timeRange === 'Daily') {
      return sorted.slice(-14).map(item => ({
        label: item.date.substring(5), // MM-DD
        focus: item.distractionFreeTimeMs / (1000 * 60 * 60),
        distraction: item.distractiveTimeMs / (1000 * 60 * 60),
      }));
    }

    if (timeRange === 'Weekly') {
      // Group by week (roughly by ISO week or just 7-day chunks from the start)
      const weeklyMap = new Map();
      sorted.forEach(item => {
        const d = new Date(item.date);
        // Get week start (Sunday)
        d.setDate(d.getDate() - d.getDay());
        const key = `${d.getMonth() + 1}/${d.getDate()}`;
        if (!weeklyMap.has(key)) weeklyMap.set(key, { count: 0, focus: 0, dist: 0 });
        const val = weeklyMap.get(key);
        val.count++;
        val.focus += item.distractionFreeTimeMs;
        val.dist += item.distractiveTimeMs;
      });
      return Array.from(weeklyMap.entries()).map(([label, val]) => ({
        label: `Wk of ${label}`,
        focus: (val.focus / val.count) / (1000 * 60 * 60),
        distraction: (val.dist / val.count) / (1000 * 60 * 60),
      })).slice(-8); // last 8 weeks
    }

    if (timeRange === 'Monthly') {
      const monthlyMap = new Map();
      sorted.forEach(item => {
        const d = new Date(item.date);
        const key = d.toLocaleString('default', { month: 'short' });
        if (!monthlyMap.has(key)) monthlyMap.set(key, { count: 0, focus: 0, dist: 0 });
        const val = monthlyMap.get(key);
        val.count++;
        val.focus += item.distractionFreeTimeMs;
        val.dist += item.distractiveTimeMs;
      });
      return Array.from(monthlyMap.entries()).map(([label, val]) => ({
        label,
        focus: (val.focus / val.count) / (1000 * 60 * 60),
        distraction: (val.dist / val.count) / (1000 * 60 * 60),
      })).slice(-12);
    }
    return [];
  }, [analyticsList, timeRange]);

  const today = analyticsList[0] || {};
  const todayFocusHours = ((today.distractionFreeTimeMs || 0) / (1000 * 60 * 60)).toFixed(1);
  const todayScreenHours = ((today.totalScreenTimeMs || 0) / (1000 * 60 * 60)).toFixed(1);
  const currentStage = userDoc.currentTreeStage || 'Seed';
  const healthRatio = userDoc.healthRatio || 1.0;

  if (loading) {
    return (
      <div className="min-h-screen bg-[#020202] flex items-center justify-center font-sans">
        <div className="w-8 h-8 rounded-full border-2 border-white/20 border-t-white animate-spin" />
      </div>
    );
  }

  // Animation variants
  const staggerContainer = {
    hidden: { opacity: 0 },
    show: {
      opacity: 1,
      transition: { staggerChildren: 0.1 }
    }
  };

  const fadeUp = {
    hidden: { opacity: 0, y: 30, filter: 'blur(8px)' },
    show: { opacity: 1, y: 0, filter: 'blur(0px)', transition: { duration: 0.8, ease: [0.32, 0.72, 0, 1] } }
  };

  return (
    <>
      <div className="bg-mesh" />
      <div className="bg-noise" />
      
      <div className="min-h-screen text-[#E0E0E0] selection:bg-white/20 font-sans selection:text-white relative z-10">
        
        {/* Navbar */}
        <motion.nav 
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, ease: "easeOut" }}
          className="fixed top-6 left-1/2 -translate-x-1/2 z-50 w-[90%] max-w-5xl"
        >
          <div className="backdrop-blur-2xl bg-[#0A0A0A]/70 border border-white/10 rounded-full h-14 flex items-center justify-between px-6 shadow-[0_8px_32px_rgba(0,0,0,0.4)]">
            <div className="flex items-center space-x-3">
              <Leaf className="w-4 h-4 text-white/70" />
              <span className="font-semibold tracking-[0.15em] text-xs uppercase text-white">Detto</span>
            </div>
            <div className="flex items-center space-x-6">
              <span className="text-xs text-white/50 hidden md:block tracking-wide">{currentUser.email}</span>
              <button 
                onClick={logout} 
                className="group w-8 h-8 rounded-full bg-white/5 hover:bg-white/10 flex items-center justify-center transition-all active:scale-95"
              >
                <LogOut className="w-3.5 h-3.5 text-white/70 group-hover:text-white group-hover:translate-x-0.5 transition-transform" />
              </button>
            </div>
          </div>
        </motion.nav>

        <main className="max-w-6xl mx-auto px-4 md:px-8 pt-40 pb-24 overflow-x-hidden">
          <motion.div variants={staggerContainer} initial="hidden" animate="show">
            
            {/* Hero Section */}
            <motion.div variants={fadeUp} className="w-full mb-24">
              <div className="flex flex-col md:flex-row items-center justify-between gap-12">
                
                <div className="flex-1 max-w-2xl z-10">
                  <div className="inline-flex items-center space-x-2 bg-white/5 border border-white/10 rounded-full px-3 py-1 mb-6 backdrop-blur-md">
                    <span className="w-1.5 h-1.5 rounded-full bg-white animate-pulse" />
                    <span className="text-[10px] font-medium tracking-[0.2em] uppercase text-white/80">Live Sync Active</span>
                  </div>
                  
                  <h1 className="text-5xl md:text-6xl lg:text-7xl font-bold tracking-tight text-white leading-[1.1] mb-6">
                    Your Digital Detox Garden
                  </h1>
                  
                  <p className="text-lg text-white/50 max-w-md leading-relaxed font-light">
                    Cultivate focus and mindfulness. Your Android progress is mathematically rendered into a living bonsai that breathes as you disconnect.
                  </p>
                </div>

                {/* The Tree Glass Enclosure */}
                <div className="z-10 w-full md:w-[450px] shrink-0">
                  <div className="p-1.5 rounded-[2.5rem] bg-gradient-to-b from-white/10 to-white/0 shadow-2xl">
                    <div className="relative w-full aspect-square bg-[#050505] rounded-[calc(2.5rem-6px)] border border-white/5 shadow-[inset_0_1px_2px_rgba(255,255,255,0.05)] overflow-hidden flex flex-col">
                      
                      <div className="absolute top-6 left-6 z-20">
                        <div className="text-[10px] uppercase tracking-widest text-white/40 mb-1">Current Stage</div>
                        <div className="text-xl font-medium text-white tracking-wide">{currentStage}</div>
                      </div>

                      <div className="flex-1 w-full h-full relative">
                        <TreeVisualizer stage={currentStage} healthRatio={healthRatio} showGlow={true} />
                      </div>
                    </div>
                  </div>
                </div>

              </div>
            </motion.div>

            {/* Stats Bento Grid */}
            <motion.div variants={fadeUp} className="mb-24">
              <h2 className="text-xl font-medium text-white mb-8 tracking-wide">Vital Metrics</h2>
              
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6 grid-flow-dense">
                <BentoCard 
                  icon={<ShieldCheck />} 
                  label="Today's Focus Time" 
                  value={`${todayFocusHours}h`} 
                  desc="Uninterrupted deep work"
                />
                <BentoCard 
                  icon={<Activity />} 
                  label="Total Screen Time" 
                  value={`${todayScreenHours}h`} 
                  desc="Monitored device usage"
                />
                <BentoCard 
                  icon={<Target />} 
                  label="Health Ratio" 
                  value={`${(healthRatio * 100).toFixed(0)}%`} 
                  desc="Tree vitality score"
                />
              </div>
            </motion.div>

            {/* Analytics Area */}
            <motion.div variants={fadeUp} className="mb-24">
              <div className="flex flex-col sm:flex-row sm:items-end justify-between mb-8 gap-6">
                <div>
                  <h2 className="text-xl font-medium text-white tracking-wide mb-2">Focus Architecture</h2>
                  <p className="text-sm text-white/50">Long-term behavioral trends vs distractions.</p>
                </div>
                
                {/* Segmented Control */}
                <div className="flex p-1 bg-white/5 rounded-full border border-white/10 backdrop-blur-md self-start sm:self-auto">
                  {['Daily', 'Weekly', 'Monthly'].map((tab) => (
                    <button
                      key={tab}
                      onClick={() => setTimeRange(tab)}
                      className={`relative px-5 py-1.5 text-xs font-medium tracking-wider uppercase transition-colors z-10 ${
                        timeRange === tab ? 'text-black' : 'text-white/60 hover:text-white'
                      }`}
                    >
                      {timeRange === tab && (
                        <motion.div
                          layoutId="activeTab"
                          className="absolute inset-0 bg-white rounded-full -z-10 shadow-sm"
                          transition={{ type: "spring", bounce: 0.2, duration: 0.5 }}
                        />
                      )}
                      {tab}
                    </button>
                  ))}
                </div>
              </div>

              {/* Chart Enclosure */}
              <div className="p-1.5 rounded-[2rem] bg-gradient-to-b from-white/10 to-transparent shadow-2xl">
                <div className="bg-[#050505] rounded-[calc(2rem-6px)] border border-white/5 shadow-[inset_0_1px_1px_rgba(255,255,255,0.05)] p-6 md:p-10 h-[450px]">
                  {chartData.length > 0 ? (
                    <ResponsiveContainer width="100%" height="100%">
                      <AreaChart data={chartData} margin={{ top: 20, right: 20, left: -20, bottom: 0 }}>
                        <defs>
                          <linearGradient id="colorFocus" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="5%" stopColor="#FFFFFF" stopOpacity={0.25}/>
                            <stop offset="95%" stopColor="#FFFFFF" stopOpacity={0}/>
                          </linearGradient>
                          <linearGradient id="colorDist" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="5%" stopColor="#444444" stopOpacity={0.4}/>
                            <stop offset="95%" stopColor="#444444" stopOpacity={0}/>
                          </linearGradient>
                        </defs>
                        <XAxis 
                          dataKey="label" 
                          stroke="#333" 
                          tick={{fill: '#888', fontSize: 11, fontWeight: 500}} 
                          tickLine={false} 
                          axisLine={false} 
                          dy={10}
                        />
                        <YAxis 
                          stroke="#333" 
                          tick={{fill: '#888', fontSize: 11}} 
                          tickLine={false} 
                          axisLine={false} 
                        />
                        <CartesianGrid strokeDasharray="3 3" stroke="#1A1A1A" vertical={false} />
                        <Tooltip content={<CustomTooltip />} />
                        <Area 
                          type="monotone" 
                          dataKey="focus" 
                          stroke="#FFFFFF" 
                          strokeWidth={2.5} 
                          fill="url(#colorFocus)" 
                          activeDot={{ r: 6, fill: '#FFF', stroke: '#000', strokeWidth: 2 }}
                        />
                        <Area 
                          type="monotone" 
                          dataKey="distraction" 
                          stroke="#666666" 
                          strokeWidth={2.5} 
                          fill="url(#colorDist)" 
                          activeDot={{ r: 6, fill: '#666', stroke: '#000', strokeWidth: 2 }}
                        />
                      </AreaChart>
                    </ResponsiveContainer>
                  ) : (
                    <div className="w-full h-full flex flex-col items-center justify-center text-white/40">
                      <BarChart2 className="w-10 h-10 mb-4 opacity-20" />
                      <p className="text-sm tracking-wide">Awaiting behavioral telemetry</p>
                    </div>
                  )}
                </div>
              </div>
            </motion.div>

          </motion.div>
        </main>
      </div>
    </>
  );
}

// ── Components ──

function BentoCard({ icon, label, value, desc }) {
  return (
    <div className="group p-1 rounded-[2rem] bg-gradient-to-b from-white/10 to-transparent hover:from-white/15 transition-colors duration-500">
      <div className="bg-[#0A0A0A] rounded-[calc(2rem-4px)] border border-white/5 shadow-[inset_0_1px_1px_rgba(255,255,255,0.05)] p-8 h-full flex flex-col relative overflow-hidden">
        <div className="absolute inset-0 bg-white/5 opacity-0 group-hover:opacity-100 transition-opacity duration-700 pointer-events-none" />
        
        <div className="flex items-center justify-between mb-8">
          <div className="w-10 h-10 rounded-full bg-white/5 flex items-center justify-center text-white/70 group-hover:scale-110 group-hover:text-white transition-all duration-500 ease-out">
            {React.cloneElement(icon, { className: "w-5 h-5" })}
          </div>
        </div>
        
        <div className="mt-auto">
          <div className="text-[10px] font-semibold text-white/40 uppercase tracking-[0.2em] mb-2">{label}</div>
          <div className="text-4xl font-bold text-white tracking-tight mb-2">{value}</div>
          <div className="text-sm text-white/50 font-light">{desc}</div>
        </div>
      </div>
    </div>
  );
}

function CustomTooltip({ active, payload, label }) {
  if (active && payload && payload.length) {
    return (
      <div className="bg-[#0F0F0F]/90 backdrop-blur-xl border border-white/10 p-4 rounded-xl shadow-2xl">
        <div className="text-xs text-white/60 uppercase tracking-widest mb-3 font-semibold">{label}</div>
        <div className="flex flex-col gap-2">
          {payload.map((entry, index) => (
            <div key={index} className="flex items-center justify-between gap-6">
              <div className="flex items-center gap-2">
                <span className="w-2 h-2 rounded-full" style={{ backgroundColor: entry.color === '#666666' ? '#666' : '#FFF' }} />
                <span className="text-sm text-white/80 capitalize">{entry.name}</span>
              </div>
              <span className="text-sm font-bold text-white">{entry.value.toFixed(1)}h</span>
            </div>
          ))}
        </div>
      </div>
    );
  }
  return null;
}
