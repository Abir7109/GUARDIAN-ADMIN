/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from "react";
import { Shield, Mail, Lock, Eye, EyeOff, ArrowRight } from "lucide-react";

interface LoginViewProps {
  onLoginSuccess: (email: string) => void;
}

export default function LoginView({ onLoginSuccess }: LoginViewProps) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");
  const [shake, setShake] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setErrorMsg("");

    setTimeout(() => {
      if (!email.includes("@")) {
        setErrorMsg("Please enter a valid administrative email address.");
        setLoading(false);
        setShake(true);
        setTimeout(() => setShake(false), 500);
        return;
      }
      
      if (password.length < 4) {
        setErrorMsg("Password must be at least 4 characters.");
        setLoading(false);
        setShake(true);
        setTimeout(() => setShake(false), 500);
        return;
      }

      setLoading(false);
      onLoginSuccess(email);
    }, 1200);
  };

  return (
    <div className="min-h-screen flex flex-col justify-center items-center relative overflow-y-auto bg-[#06050e] text-white w-full py-8 md:py-16">
      {/* Background Dots Grid Pattern */}
      <div 
        className="absolute inset-0 z-0 pointer-events-none opacity-5"
        style={{
          backgroundSize: "28px 28px",
          backgroundImage: `radial-gradient(#6122e6 1.5px, transparent 1.5px)`
        }}
      />
      {/* Ambient Glow behind card */}
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-[#6122e6]/10 rounded-full blur-[100px] z-0 pointer-events-none" />

      {/* Main Container */}
      <main className="relative z-10 w-full px-4 flex justify-center items-center">
        {/* Login Card with stitch-style crisp border */}
        <div 
          className={`bg-[#0c0b18] border border-[#1e1c31] w-full max-w-[420px] rounded-2xl p-10 flex flex-col items-center transition-all duration-200 shadow-2xl backdrop-blur-xl my-auto ${
            shake ? "shake-anim" : ""
          }`}
        >
          {/* Logo area */}
          <div className="w-16 h-16 rounded-xl bg-[#6122e6] flex items-center justify-center mb-6 shadow-[0_4px_16px_rgba(97,34,230,0.4)]">
            <Shield className="text-white w-8 h-8" />
          </div>

          {/* Header Text */}
          <h1 className="font-sans text-2xl font-bold tracking-tight text-white mb-2">
            Guardian — Anti-Theft
          </h1>
          <p className="font-sans text-sm text-[#8e8a9f] mb-6 text-center">
            Administrative Command Center
          </p>

          {/* Form */}
          {errorMsg && (
            <div className="w-full bg-[#fc2e5c]/10 border-l-[3px] border-[#fc2e5c] p-3 rounded-r-lg mb-6 flex items-start gap-3 justify-start text-left">
              <Shield className="text-[#fc2e5c] w-5 h-5 shrink-0 mt-0.5" />
              <p className="font-sans text-xs text-white leading-normal font-medium">
                {errorMsg}
              </p>
            </div>
          )}

          {/* Form */}
          <form className="w-full flex flex-col gap-5" onSubmit={handleSubmit}>
            {/* Email Input */}
            <div className="flex flex-col gap-1.5 text-left">
              <label className="font-sans text-[11px] font-bold text-[#8e8a9f] uppercase tracking-wider" htmlFor="email">
                Email Address
              </label>
              <div className="relative flex items-center rounded-xl border border-[#1e1c31] bg-[#131127] transition-all duration-200 focus-within:border-[#6122e6] focus-within:ring-1 focus-within:ring-[#6122e6]/50">
                <Mail className="absolute left-3.5 text-[#8e8a9f]/50 w-5 h-5" />
                <input
                  className="w-full bg-transparent border-none outline-none text-white text-sm pl-11 pr-3 py-3.5 focus:ring-0 placeholder:text-[#8e8a9f]/30"
                  id="email"
                  name="email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="admin@stitch.com"
                  required
                  disabled={loading}
                />
              </div>
            </div>

            {/* Password Input */}
            <div className="flex flex-col gap-1.5 text-left">
              <label className="font-sans text-[11px] font-bold text-[#8e8a9f] uppercase tracking-wider" htmlFor="password">
                Password
              </label>
              <div className="relative flex items-center rounded-xl border border-[#1e1c31] bg-[#131127] transition-all duration-200 focus-within:border-[#6122e6] focus-within:ring-1 focus-within:ring-[#6122e6]/50">
                <Lock className="absolute left-3.5 text-[#8e8a9f]/50 w-5 h-5" />
                <input
                  className="w-full bg-transparent border-none outline-none text-white text-sm pl-11 pr-12 py-3.5 focus:ring-0 placeholder:text-[#8e8a9f]/30"
                  id="password"
                  name="password"
                  type={showPassword ? "text" : "password"}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  required
                  disabled={loading}
                />
                <button
                  className="absolute right-3 text-[#8e8a9f]/60 hover:text-white transition-colors flex items-center justify-center cursor-pointer"
                  onClick={() => setShowPassword(!showPassword)}
                  type="button"
                  disabled={loading}
                >
                  {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
            </div>

            {/* Options Row */}
            <div className="flex items-center justify-between w-full mt-1">
              <label className="flex items-center gap-2 cursor-pointer group select-none">
                <input
                  className="w-4 h-4 rounded border-[#1e1c31] bg-[#131127] text-[#6122e6] focus:ring-[#6122e6] focus:ring-offset-[#0c0b18] focus:ring-offset-2 transition-all cursor-pointer"
                  type="checkbox"
                  disabled={loading}
                />
                <span className="font-sans text-xs text-[#8e8a9f] group-hover:text-white transition-colors">
                  Remember me
                </span>
              </label>
              <a 
                className="font-sans text-xs text-[#6122e6] hover:text-[#7c3aed] transition-colors cursor-pointer"
                onClick={() => alert("Please contact Abir Specialist to reset the key card credentials.")}
              >
                Forgot password?
              </a>
            </div>

            {/* Submit Button */}
            <button
              className="w-full bg-[#6122e6] text-white font-sans text-sm font-semibold py-3.5 rounded-xl hover:bg-[#501bc5] transition-all duration-200 mt-4 flex items-center justify-center gap-2 cursor-pointer shadow-[0_4px_20px_rgba(97,34,230,0.35)] disabled:opacity-80"
              type="submit"
              disabled={loading}
            >
              <span>{loading ? "Verifying Session..." : "Authenticate"}</span>
              <ArrowRight className={`w-4.5 h-4.5 ${loading ? "animate-spin" : ""}`} />
            </button>
          </form>

          {/* Footer / Contact Info */}
          <div className="mt-8 pt-6 border-t border-[#1e1c31] w-full text-center">
            <p className="font-sans text-xs text-[#8e8a9f]/70 flex items-center justify-center gap-1.5 font-medium">
              Guardian Security Core v1.0
            </p>
          </div>
        </div>
      </main>

      <style>{`
        .shake-anim {
          animation: shake 0.4s ease-in-out;
        }
        @keyframes shake {
          0%, 100% { transform: translateX(0); }
          20%, 60% { transform: translateX(-6px); }
          40%, 80% { transform: translateX(6px); }
        }
      `}</style>
    </div>
  );
}
