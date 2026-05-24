/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

export default function AnalyticsView() {
  return (
    <div className="flex flex-col gap-6 w-full max-w-[1400px] mx-auto animate-fade-in text-[#d9e3f5]">
      
      {/* Top filter select header */}
      <section className="bg-[#141b25]/80 border border-[#2a3441] rounded-xl p-5 flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="flex flex-col text-left">
          <h3 className="font-sans font-bold text-base text-[#d9e3f5]">
            Telemetry Analytics Command Center
          </h3>
          <span className="font-sans text-xs text-[#b9cbb9]/50 mt-1">
            Real-time subscriber bandwidth metrics, device OS segmentations, and regional routing maps.
          </span>
        </div>

        <div className="flex items-center gap-3 w-full sm:w-auto shrink-0 select-none justify-end">
          <span className="font-mono text-xs text-[#b9cbb9]/50 uppercase tracking-widest font-semibold mr-2">Range:</span>
          <select className="bg-[#050f1b] border border-[#2a3441] rounded-lg px-4 py-2 outline-none font-sans text-xs focus:border-[#00ff88]/50 text-white cursor-pointer hover:bg-[#1a232e]">
            <option value="7d">Last 7 Days (Detailed)</option>
            <option value="30d">Last 30 Days (Aggregate)</option>
            <option value="24h">Last 24 Hours (Realtime)</option>
          </select>
        </div>
      </section>

      {/* Main Grid: Analytical Cards */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        
        {/* KPI 1: Active Daily Subscriber curve (SVG detailed area chart) */}
        <div className="bg-[#141b25]/80 border border-[#2a3441] rounded-xl p-6 flex flex-col">
          <div className="flex justify-between items-start mb-6">
            <div className="flex flex-col text-left">
              <span className="font-mono text-[9px] text-[#b9cbb9]/50 uppercase tracking-wider font-semibold">Active Directories</span>
              <h4 className="font-sans font-bold text-sm text-[#d9e3f5] mt-0.5">DAU / MAU Core Ratio Growth</h4>
            </div>
            <span className="font-mono text-xs text-[#00ff88] font-bold uppercase p-1 rounded bg-[#00ff88]/15 border border-[#3b4b3d]">
              92.4% Retention
            </span>
          </div>

          {/* SVG detailed double line chart with precise grids */}
          <div className="w-full h-52 relative mt-2 select-none">
            <svg className="w-full h-full" viewBox="0 0 500 180" preserveAspectRatio="none">
              <defs>
                <linearGradient id="dau-glow" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#00a3ff" stopOpacity="0.2" />
                  <stop offset="100%" stopColor="#00a3ff" stopOpacity="0.0" />
                </linearGradient>
              </defs>
              {/* Grid Horizontal */}
              <line x1="0" y1="45" x2="500" y2="45" stroke="#2a3441" strokeWidth="0.5" strokeDasharray="3 3" />
              <line x1="0" y1="90" x2="500" y2="90" stroke="#2a3441" strokeWidth="0.5" strokeDasharray="3 3" />
              <line x1="0" y1="135" x2="500" y2="135" stroke="#2a3441" strokeWidth="0.5" strokeDasharray="3 3" />

              {/* Area DAU */}
              <path
                d="M 0 180 Q 80 120, 160 140 T 320 80 T 420 40 T 500 20 L 500 180 Z"
                fill="url(#dau-glow)"
              />

              {/* MAU Line (Slate line) */}
              <path
                d="M 0 160 Q 80 130, 160 120 T 320 90 T 420 50 T 500 45"
                fill="none"
                stroke="#d9e3f5"
                strokeWidth="2"
                strokeDasharray="4 4"
                strokeOpacity="0.6"
              />

              {/* DAU Line (Bright Blue) */}
              <path
                d="M 0 180 Q 80 120, 160 140 T 320 80 T 420 40 T 500 20"
                fill="none"
                stroke="#00a3ff"
                strokeWidth="2.5"
                strokeLinecap="round"
              />
            </svg>

            {/* Timestamps */}
            <div className="flex justify-between items-center w-full mt-4 border-t border-[#2a3441]/50 pt-2 font-mono text-[9px] text-[#b9cbb9]/50">
              <span>May 18</span>
              <span>May 20</span>
              <span>May 22</span>
              <span>May 24 (Today)</span>
            </div>
          </div>
        </div>

        {/* KPI 2: Device Model OS distribution (Vertical structured column bars) */}
        <div className="bg-[#141b25]/80 border border-[#2a3441] rounded-xl p-6 flex flex-col">
          <div className="flex justify-between items-start mb-6">
            <div className="flex flex-col text-left">
              <span className="font-mono text-[9px] text-[#b9cbb9]/50 uppercase tracking-wider font-semibold">Subscriber Models</span>
              <h4 className="font-sans font-bold text-sm text-[#d9e3f5] mt-0.5">Enrolled hardware OS Split</h4>
            </div>
            <span className="font-mono text-xs text-teal-400 font-bold p-1 rounded bg-[#14b8a6]/10 border border-[#14b8a6]/30">
              SecOS Premier
            </span>
          </div>

          <div className="flex items-end justify-between h-52 pt-4 px-4 select-none">
            {/* Bar 1 */}
            <div className="flex flex-col items-center gap-3 w-[15%]">
              <span className="font-mono text-[10px] text-[#00ff88] font-bold">52%</span>
              <div className="w-8 bg-gradient-to-t from-[#025a34] to-[#00ff88] rounded-t-md h-28 border border-[#3b4b3d]" />
              <span className="font-sans text-[10px] text-[#b9cbb9]/60 truncate max-w-full">SP-Alpha</span>
            </div>
            {/* Bar 2 */}
            <div className="flex flex-col items-center gap-3 w-[15%]">
              <span className="font-mono text-[10px] text-teal-400 font-bold">28%</span>
              <div className="w-8 bg-gradient-to-t from-teal-900 to-teal-400 rounded-t-md h-[72px] border border-teal-700/30" />
              <span className="font-sans text-[10px] text-[#b9cbb9]/60 truncate max-w-full">SP-Beta</span>
            </div>
            {/* Bar 3 */}
            <div className="flex flex-col items-center gap-3 w-[15%]">
              <span className="font-mono text-[10px] text-blue-400 font-bold">12%</span>
              <div className="w-8 bg-gradient-to-t from-blue-900 to-blue-400 rounded-t-md h-8 border border-blue-700/30" />
              <span className="font-sans text-[10px] text-[#b9cbb9]/60 truncate max-w-full">SP-Gamma</span>
            </div>
            {/* Bar 4 */}
            <div className="flex flex-col items-center gap-3 w-[15%]">
              <span className="font-mono text-[10px] text-[#ffea2a] font-bold">8%</span>
              <div className="w-8 bg-gradient-to-t from-[#70640e] to-[#ffea2a] rounded-t-md h-5 border border-[#837617]/50" />
              <span className="font-sans text-[10px] text-[#b9cbb9]/60 truncate max-w-full font-bold">Legacy OS</span>
            </div>
          </div>
        </div>

        {/* KPI 3: Onboarding Pipeline Funnel */}
        <div className="bg-[#141b25]/80 border border-[#2a3441] rounded-xl p-6 flex flex-col">
          <h4 className="font-sans font-bold text-sm text-[#d9e3f5] mb-6 text-left">
            Remote Device Enrollment Funnel
          </h4>

          {/* Vertical segmented conversion funnel list */}
          <div className="flex flex-col gap-4 mt-1 select-none">
            {/* Segment 1 */}
            <div className="flex items-center justify-between text-xs">
              <div className="flex flex-col text-left gap-1 truncate max-w-[200px]">
                <span className="font-sans font-semibold text-[#d9e3f5]">1. Enrolled Directory</span>
                <span className="font-mono text-[10px] text-[#b9cbb9]/40">Initial activation key assigned</span>
              </div>
              <div className="flex items-center gap-3 justify-end">
                <span className="font-mono text-xs font-semibold text-[#b9cbb9]">1,244 Devs</span>
                <div className="w-36 h-2 rounded bg-gray-950/50 border border-[#2a3441] overflow-hidden">
                  <div className="h-full bg-blue-500" style={{ width: "100%" }} />
                </div>
                <span className="font-mono text-[10px] text-blue-400 font-extrabold w-8">100%</span>
              </div>
            </div>

            {/* Segment 2 */}
            <div className="flex items-center justify-between text-xs">
              <div className="flex flex-col text-left gap-1 truncate max-w-[200px]">
                <span className="font-sans font-semibold text-[#d9e3f5]">2. Biometrics Connected</span>
                <span className="font-mono text-[10px] text-[#b9cbb9]/40">Physical thumbprints mapped</span>
              </div>
              <div className="flex items-center gap-3 justify-end">
                <span className="font-mono text-xs font-semibold text-[#b9cbb9]">1,085 Devs</span>
                <div className="w-36 h-2 rounded bg-gray-950/50 border border-[#2a3441] overflow-hidden">
                  <div className="h-full bg-[#00ff88]" style={{ width: "87%" }} />
                </div>
                <span className="font-mono text-[10px] text-[#00ff88] font-extrabold w-8">87%</span>
              </div>
            </div>

            {/* Segment 3 */}
            <div className="flex items-center justify-between text-xs">
              <div className="flex flex-col text-left gap-1 truncate max-w-[200px]">
                <span className="font-sans font-semibold text-[#d9e3f5]">3. Shield Protection Active</span>
                <span className="font-mono text-[10px] text-[#b9cbb9]/40">Active encryption guards engaged</span>
              </div>
              <div className="flex items-center gap-3 justify-end">
                <span className="font-mono text-xs font-semibold text-[#b9cbb9]">957 Devs</span>
                <div className="w-36 h-2 rounded bg-gray-950/50 border border-[#2a3441] overflow-hidden">
                  <div className="h-full bg-teal-400" style={{ width: "77%" }} />
                </div>
                <span className="font-mono text-[10px] text-teal-400 font-extrabold w-8">77%</span>
              </div>
            </div>

            {/* Segment 4 */}
            <div className="flex items-center justify-between text-xs">
              <div className="flex flex-col text-left gap-1 truncate max-w-[200px]">
                <span className="font-sans font-semibold text-[#d9e3f5]">4. Enterprise Verified</span>
                <span className="font-mono text-[10px] text-[#b9cbb9]/40">Enforced centralized policies sync</span>
              </div>
              <div className="flex items-center gap-3 justify-end">
                <span className="font-mono text-xs font-semibold text-[#b9cbb9]">812 Devs</span>
                <div className="w-36 h-2 rounded bg-gray-950/50 border border-[#2a3441] overflow-hidden">
                  <div className="h-full bg-[#ffea2a]" style={{ width: "65%" }} />
                </div>
                <span className="font-mono text-[10px] text-[#ffea2a] font-extrabold w-8">65%</span>
              </div>
            </div>
          </div>
        </div>

        {/* KPI 4: Vector Region routing connection globe mockup map */}
        <div className="bg-[#141b25]/80 border border-[#2a3441] rounded-xl p-6 flex flex-col">
          <h4 className="font-sans font-bold text-sm text-[#d9e3f5] mb-4 text-left">
            Regional Router Hotspots Map
          </h4>

          {/* Grid representation styled with beautiful visual accents */}
          <div className="flex-1 min-h-[140px] border border-[#232d3a] bg-[#050f1b] rounded-lg relative overflow-hidden flex flex-col p-4 select-none justify-center items-center">
            
            {/* Minimal SVG map visualization mock */}
            <svg className="w-full h-24 stroke-[#2a3441] stroke-1" fill="none" viewBox="0 0 400 100">
              {/* Fake outline of world continents */}
              <path d="M 30,30 Q 50,55 80,45 T 150,20 T 200,60 T 240,40" strokeWidth="0.5" strokeDasharray="2 4" />
              <path d="M 280,20 Q 320,40 370,30 Q 390,65 350,85" strokeWidth="0.5" strokeDasharray="2 4" />
              
              {/* Radial signal wave on Europe */}
              <circle cx="180" cy="35" r="4" fill="#00ff88" />
              <circle cx="180" cy="35" r="10" fill="none" stroke="#00ff88" strokeWidth="0.5" strokeOpacity="0.4" className="animate-ping" />

              {/* Radial signal on USA */}
              <circle cx="80" cy="40" r="4" fill="#00a3ff" />
              <circle cx="80" cy="40" r="12" fill="none" stroke="#00a3ff" strokeWidth="0.5" strokeOpacity="0.3" />

              {/* Connecting dashed arc line */}
              <path d="M 80,40 Q 130,10 180,35" stroke="#00ff88" strokeWidth="1" strokeDasharray="3 3" strokeOpacity="0.8" />
            </svg>

            <div className="flex justify-between items-center w-full mt-4 font-mono text-[9px] text-[#b9cbb9]/50">
              <span className="flex items-center gap-1.5">
                <span className="w-2 h-2 rounded-full bg-[#00ff88] block" />
                EMEA: 452 Subscriptions
              </span>
              <span className="flex items-center gap-1.5">
                <span className="w-2 h-2 rounded-full bg-[#00a3ff] block" />
                AMER: 360 Subscriptions
              </span>
            </div>

          </div>
        </div>

      </div>

    </div>
  );
}
