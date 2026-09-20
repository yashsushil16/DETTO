import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Loader2 } from 'lucide-react';

export default function AuthGateway() {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [msg, setMsg] = useState('');
  const [loading, setLoading] = useState(false);
  
  const { login, signup, resetPassword } = useAuth();
  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setMsg('');
    
    if (!isLogin && password !== confirmPassword) {
      return setError('Passwords do not match');
    }

    setLoading(true);

    try {
      if (isLogin) {
        await login(email, password);
      } else {
        await signup(email, password);
      }
      navigate('/');
    } catch (err) {
      setError(err.message || 'Failed to authenticate');
    }
    setLoading(false);
  }

  async function handleResetPassword() {
    if (!email) {
      return setError('Please enter your email first to reset password');
    }
    try {
      setError('');
      setLoading(true);
      await resetPassword(email);
      setMsg('Password reset email sent. Check your inbox.');
    } catch (err) {
      setError('Failed to reset password: ' + err.message);
    }
    setLoading(false);
  }

  return (
    <div className="min-h-screen bg-[#050505] text-[#E0E0E0] flex flex-col items-center justify-center p-4 overflow-hidden relative font-sans">
      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.8, ease: "easeOut" }}
        className="w-full max-w-sm z-10"
      >
        <div className="text-center mb-10">
          <motion.div
            initial={{ scale: 0.9, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ delay: 0.2, duration: 0.5 }}
            className="text-4xl font-black mb-1 text-white tracking-widest"
          >
            DETTO
          </motion.div>
          <p className="text-[#888888] text-xs tracking-[0.3em] uppercase">Cloud Sync</p>
        </div>

        <div className="bg-[#0A0A0A] border border-[#222222] rounded-xl p-8 shadow-2xl">
          <h2 className="text-xl font-semibold mb-6 text-center text-white">
            {isLogin ? 'Sign In' : 'Register'}
          </h2>
          
          <AnimatePresence>
            {error && (
              <motion.div initial={{opacity:0, height:0}} animate={{opacity:1, height:'auto'}} exit={{opacity:0, height:0}} className="bg-[#1A0A0A] border border-[#441111] text-[#FF6B6B] text-xs p-3 rounded mb-5 text-center">
                {error}
              </motion.div>
            )}
            {msg && (
              <motion.div initial={{opacity:0, height:0}} animate={{opacity:1, height:'auto'}} exit={{opacity:0, height:0}} className="bg-[#0A1A0A] border border-[#114411] text-[#6BFF6B] text-xs p-3 rounded mb-5 text-center">
                {msg}
              </motion.div>
            )}
          </AnimatePresence>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs text-[#888888] mb-1 uppercase tracking-wider">Email</label>
              <input 
                type="email" 
                required 
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full bg-[#111111] border border-[#333333] rounded px-4 py-2.5 text-white text-sm focus:outline-none focus:border-[#666666] transition-colors"
              />
            </div>
            
            <div>
              <label className="block text-xs text-[#888888] mb-1 uppercase tracking-wider">Password</label>
              <input 
                type="password" 
                required 
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full bg-[#111111] border border-[#333333] rounded px-4 py-2.5 text-white text-sm focus:outline-none focus:border-[#666666] transition-colors"
              />
            </div>

            {!isLogin && (
              <motion.div initial={{opacity:0, height:0}} animate={{opacity:1, height:'auto'}}>
                <label className="block text-xs text-[#888888] mb-1 uppercase tracking-wider mt-4">Confirm Password</label>
                <input 
                  type="password" 
                  required 
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="w-full bg-[#111111] border border-[#333333] rounded px-4 py-2.5 text-white text-sm focus:outline-none focus:border-[#666666] transition-colors"
                />
              </motion.div>
            )}

            <button 
              disabled={loading}
              type="submit"
              className="w-full bg-white text-black font-semibold text-sm py-2.5 rounded mt-6 hover:bg-[#E0E0E0] transition-colors flex justify-center items-center disabled:opacity-70"
            >
              {loading ? <Loader2 className="animate-spin w-4 h-4" /> : (isLogin ? 'Continue' : 'Register')}
            </button>
          </form>

          <div className="mt-6 flex flex-col items-center space-y-3">
            {isLogin && (
              <button 
                onClick={handleResetPassword}
                type="button"
                className="text-xs text-[#666666] hover:text-white transition-colors"
              >
                Forgot Password?
              </button>
            )}
            
            <button 
              onClick={() => {
                setIsLogin(!isLogin);
                setError('');
                setMsg('');
              }} 
              type="button"
              className="text-xs text-[#888888] hover:text-white transition-colors"
            >
              {isLogin ? 'Create an account' : 'Already have an account? Sign In'}
            </button>
          </div>
        </div>
      </motion.div>
    </div>
  );
}
