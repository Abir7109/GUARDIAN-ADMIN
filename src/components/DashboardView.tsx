/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { EventLog, SecurityUser, LiveFeedItem } from "../types";
import {
  AlertTriangle,
  Smartphone,
  Radio,
  Shield,
  ArrowUpRight,
  Activity,
  CheckCircle2,
  AlertCircle
} from "lucide-react";

interface DashboardViewProps {
  users: SecurityUser[];
  logs: EventLog[];
  feed: LiveFeedItem[];
  apiCount: number;
  maintenanceMode: boolean;
  onOpenLogDetails: (log: EventLog) => void;
  onNavigateToTab: (tab: string) => void;
}

export default function DashboardView({
  users,
  logs,
  feed,
  apiCount,
  maintenanceMode,
  onOpenLogDetails,
  onNavigateToTab
}: DashboardViewProps) {
  // Compute analytics numbers dynamically from actual DB
  const totalUsersCount = users.length;
  const activeUsersCount = users.filter((u) => u.status === "Active").length;
  const protectedCount = users.filter((u) => u.protectionActive).length;
  const activeTriggersCount = logs.filter((l) => l.type === "Trigger").length;

  // Pie segment calculations for threat sectors
  const triggerTypes = {
    Trigger: logs.filter((l) => l.type === "Trigger").length,
    Alarm: logs.filter((l) => l.type === "Alarm").length,
    Access: logs.filter((l) => l.type === "Access").length
  };
  const totalLogs = logs.length || 1;
  const triggerPct = Math.round((triggerTypes.Trigger / totalLogs) * 100);
  const alarmPct = Math.round((triggerTypes.Alarm / totalLogs) * 100);
  const accessPct = Math.round((triggerTypes.Access / totalLogs) * 100);

  return (
    <div className="flex flex-col gap-6 w-full max-w-[1400px] mx-auto animate-fade-in text-white">
      
      {/* Banner Notice if under Maintenance or global alerts are on */}
      {maintenanceMode && (
        <div className="bg-[#fc2e5c]/10 border border-[#fc2e5c]/25 rounded-2xl px-5 py-4 flex items-center justify-between gap-4">
          <div className="flex items-center gap-3 text-left">
            <AlertTriangle className="text-[#ff8ba4] w-6 h-6 shrink-0" />
            <div className="flex flex-col">
              <span className="font-sans font-bold text-sm text-[#ff8ba4]">
                Emergency Protocol Configured
              </span>
              <p className="font-sans text-xs text-[#8e8a9f] leading-tight mt-0.5">
                Guardian systems are running under degraded state. Offline redundancy backups active.
              </p>
            </div>
          </div>
          <button 
            onClick={() => onNavigateToTab("app-config")}
            className="px-3.5 py-1.5 border border-[#ff8ba4]/30 bg-[#ff8ba4]/10 text-[#ff8ba4] font-sans text-xs font-semibold rounded-xl hover:bg-[#ff8ba4]/20 transition-all cursor-pointer shrink-0"
          >
            Manage Protocol
          </button>
        </div>
      )}

      {/* KPI Stats Grid */}
      <section className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5" id="kpi-stats">
        
        {/* Metric 1 - Total Subscribing Fleet */}
        <div className="bg-[#0c0b18] border border-[#1e1c31] rounded-2xl p-5 hover:border-[#6122e6]/50 transition-all duration-300 shadow-md relative group flex flex-col justify-between">
          <div className="flex items-start justify-between">
            <div className="flex flex-col text-left">
              <span className="font-sans text-xs text-[#8e8a9f] font-semibold">
                Total Registered Devices
              </span>
              <span className="font-sans font-bold text-3xl text-white mt-1 tracking-tight">
                {totalUsersCount}
              </span>
            </div>
            <div className="w-10 h-10 bg-[#6122e6]/10 border border-[#6122e6]/20 rounded-xl flex items-center justify-center text-[#a78bfa]">
              <Smartphone className="w-5.5 h-5.5" />
            </div>
          </div>
          <div className="mt-4 flex items-center gap-2">
            <span className="font-sans text-xs text-[#00f59b] flex items-center gap-0.5 font-bold">
              <ArrowUpRight className="w-3.5 h-3.5" />
              +12.4%
            </span>
            <span className="font-sans text-[11px] text-[#8e8a9f]/60">
              vs trailing 7 days
            </span>
          </div>
        </div>

        {/* Metric 2 - Active Today */}
        <div className="bg-[#0c0b18] border border-[#1e1c31] rounded-2xl p-5 hover:border-[#6122e6]/50 transition-all duration-300 shadow-md relative group flex flex-col justify-between">
          <div className="flex items-start justify-between">
            <div className="flex flex-col text-left">
              <span className="font-sans text-xs text-[#8e8a9f] font-semibold">
                Active Devices Today
              </span>
              <span className="font-sans font-bold text-3xl text-white mt-1 tracking-tight">
                {activeUsersCount}
              </span>
            </div>
            <div className="w-10 h-10 bg-[#00f59b]/10 border border-[#00f59b]/20 rounded-xl flex items-center justify-center text-[#00f59b]">
              <Radio className="w-5.5 h-5.5 animate-pulse" />
            </div>
          </div>
          <div className="mt-4 flex items-center gap-2">
            <span className="font-sans text-xs text-[#00f59b] flex items-center gap-0.5 font-bold">
              <CheckCircle2 className="w-3.5 h-3.5" />
              {Math.round((activeUsersCount / totalUsersCount) * 100)}%
            </span>
            <span className="font-sans text-[11px] text-[#8e8a9f]/60">
              online concurrently
            </span>
          </div>
        </div>

        {/* Metric 3 - Threat Triggers */}
        <div className="bg-[#0c0b18] border border-[#1e1c31] rounded-2xl p-5 hover:border-[#6122e6]/50 transition-all duration-300 shadow-md relative group flex flex-col justify-between">
          <div className="flex items-start justify-between">
            <div className="flex flex-col text-left">
              <span className="font-sans text-xs text-[#8e8a9f] font-semibold">
                Threat Triggers Activated
              </span>
              <span className="font-sans font-bold text-3xl text-[#ff8ba4] mt-1 tracking-tight">
                {activeTriggersCount}
              </span>
            </div>
            <div className="w-10 h-10 bg-[#fc2e5c]/10 border border-[#fc2e5c]/25 rounded-xl flex items-center justify-center text-[#ff8ba4]">
              <AlertCircle className="w-5.5 h-5.5" />
            </div>
          </div>
          <div className="mt-4 flex items-center gap-2">
            <span className="font-sans text-xs text-[#ff8ba4] flex items-center gap-0.5 font-semibold">
              <AlertTriangle className="w-3.5 h-3.5" />
              Requires Review
            </span>
            <span className="font-sans text-[11px] text-[#8e8a9f]/60">
              unresolved traces
            </span>
          </div>
        </div>

        {/* Metric 4 - Remotely Guarded */}
        <div className="bg-[#0c0b18] border border-[#1e1c31] rounded-2xl p-5 hover:border-[#6122e6]/50 transition-all duration-300 shadow-md relative group flex flex-col justify-between">
          <div className="flex items-start justify-between">
            <div className="flex flex-col text-left">
              <span className="font-sans text-xs text-[#8e8a9f] font-semibold">
                Guarded Fleet Shield
              </span>
              <span className="font-sans font-bold text-3xl text-white mt-1 tracking-tight">
                {protectedCount}
              </span>
            </div>
            <div className="w-10 h-10 bg-[#6122e6]/10 border border-[#6122e6]/20 rounded-xl flex items-center justify-center text-[#c084fc]">
              <Shield className="w-5.5 h-5.5" />
            </div>
          </div>
          <div className="mt-4 flex items-center gap-2">
            <span className="font-sans text-xs text-[#00f59b] flex items-center gap-0.5 font-bold">
              <CheckCircle2 className="w-3.5 h-3.5" />
              {Math.round((protectedCount / totalUsersCount) * 100)}%
            </span>
            <span className="font-sans text-[11px] text-[#8e8a9f]/60">
              shields running
            </span>
          </div>
        </div>

      </section>

      {/* Main content grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 text-left">
        
        {/* Chart Column and Triggers - 2 cols wide on desktop */}
        <div className="lg:col-span-2 flex flex-col gap-6">
          
          {/* Real-time telemetry chart section */}
          <div className="bg-[#0c0b18] border border-[#1e1c31] rounded-2xl p-6 relative shadow-md">
            <div className="flex items-center justify-between mb-4">
              <div className="flex flex-col">
                <h3 className="font-sans font-bold text-sm text-white">
                  Platform Operations Volume
                </h3>
                <span className="font-sans text-xs text-[#8e8a9f]">
                  Calculated transaction load per hour (24h period)
                </span>
              </div>
              <div className="flex items-center gap-2">
                <span className="w-2.5 h-2.5 rounded-full bg-[#00f59b] block animate-pulse" />
                <span className="font-sans text-[11px] text-[#00f59b] font-bold">
                  {apiCount.toLocaleString()} Total Operations
                </span>
              </div>
            </div>

            {/* Custom SVG telemetry line chart with absolute professional grace */}
            <div className="w-full h-44 mt-6 flex flex-col justify-end relative select-none">
              <svg className="absolute inset-0 w-full h-full" viewBox="0 0 600 150"  preserveAspectRatio="none">
                <defs>
                  <linearGradient id="chart-glow" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#6122e6" stopOpacity="0.25" />
                    <stop offset="100%" stopColor="#6122e6" stopOpacity="0.0" />
                  </linearGradient>
                </defs>
                {/* Horizontal Grid lines */}
                <line x1="0" y1="37.5" x2="600" y2="37.5" stroke="#1e1c31" strokeWidth="1" strokeDasharray="4 4" />
                <line x1="0" y1="75" x2="600" y2="75" stroke="#1e1c31" strokeWidth="1" strokeDasharray="4 4" />
                <line x1="0" y1="112.5" x2="600" y2="112.5" stroke="#1e1c31" strokeWidth="1" strokeDasharray="4 4" />

                {/* Filled Glow below line */}
                <path
                  d="M 0 150 L 0 110 L 50 120 L 100 80 L 150 95 L 200 45 L 250 60 L 300 25 L 350 40 L 400 90 L 450 70 L 500 15 L 550 20 L 600 5 C 600 50, 600 100, 600 150 Z"
                  fill="url(#chart-glow)"
                />
                {/* Actual stroke line */}
                <path
                  d="M 0 110 L 50 120 L 100 80 L 150 95 L 200 45 L 250 60 L 300 25 L 350 40 L 400 90 L 450 70 L 500 15 L 550 20 L 600 5"
                  fill="none"
                  stroke="#6122e6"
                  strokeWidth="2.5"
                  strokeLinecap="round"
                />
              </svg>

              {/* X Axes descriptors */}
              <div className="flex justify-between items-center w-full mt-4 border-t border-[#1e1c31] pt-2 font-mono text-[9px] text-[#8e8a9f]">
                <span>00:00</span>
                <span>04:00</span>
                <span>08:00</span>
                <span>12:00</span>
                <span>16:00</span>
                <span>20:00</span>
              </div>
            </div>
          </div>

          {/* Recent Triggers Table list item */}
          <div className="bg-[#0c0b18] border border-[#1e1c31] rounded-2xl p-6 shadow-md">
            <div className="flex items-center justify-between mb-4">
              <div className="flex flex-col">
                <h3 className="font-sans font-bold text-sm text-white">
                  Active Security Incidents & Flags
                </h3>
                <span className="font-sans text-xs text-[#8e8a9f]">
                  Recent security traces logged across subscriber fleet
                </span>
              </div>
              <button
                onClick={() => onNavigateToTab("event-logs")}
                className="font-sans text-xs text-[#6122e6] hover:text-[#763bf2] transition-all flex items-center gap-1 cursor-pointer font-semibold"
              >
                <span>Launch Logs Area</span>
                <ArrowUpRight className="w-3.5 h-3.5" />
              </button>
            </div>

            {/* Logs Table */}
            <div className="overflow-x-auto">
              <table className="w-full text-left font-sans text-xs">
                <thead>
                  <tr className="border-b border-[#1e1c31] text-[#8e8a9f] font-sans text-[11px] uppercase tracking-wider font-semibold">
                    <th className="py-3">Type</th>
                    <th className="py-3">Interface</th>
                    <th className="py-3">Status Description</th>
                    <th className="py-3">Node Location</th>
                    <th className="py-3 text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#1e1c31]/60">
                  {logs.slice(0, 4).map((log) => {
                    const isTrigger = log.type === "Trigger";
                    const isAlarm = log.type === "Alarm";

                    return (
                      <tr key={log.id} className="hover:bg-[#131127]/60 transition-colors group">
                        <td className="py-3.5">
                          <span
                            className={`px-2.5 py-1 rounded-lg font-sans text-[10px] font-semibold ${
                              isTrigger
                                ? "bg-[#fc2e5c]/10 text-[#ff8ba4] border border-[#fc2e5c]/20"
                                : isAlarm
                                ? "bg-amber-400/10 text-amber-300 border border-amber-400/20"
                                : "bg-[#00f59b]/10 text-[#00f59b] border border-[#00f59b]/20"
                            }`}
                          >
                            {log.type}
                          </span>
                        </td>
                        <td className="py-3.5 font-mono text-white text-xs">
                          {log.source}
                        </td>
                        <td className="py-3.5 text-[#8e8a9f] font-medium max-w-[200px] truncate">
                          {log.details}
                        </td>
                        <td className="py-3.5 text-[#8e8a9f]/80 font-mono text-[11px]">
                          {log.location}
                        </td>
                        <td className="py-3.5 text-right">
                          <button
                            onClick={() => onOpenLogDetails(log)}
                            className="font-sans text-xs bg-[#131127] text-[#8e8a9f] hover:text-white px-3 py-1.5 rounded-xl border border-[#252243] hover:bg-[#6c26f0] hover:text-white hover:border-[#6c26f0] transition-all cursor-pointer font-semibold"
                          >
                            Investigate
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

          </div>

        </div>

        {/* Audit Metrics Sidebar & Live Feed */}
        <div className="flex flex-col gap-6">
          
          {/* Threat circle segmented widget */}
          <div className="bg-[#0c0b18] border border-[#1e1c31] rounded-2xl p-6 flex flex-col shadow-md">
            <h3 className="font-sans font-bold text-sm text-white mb-5">
              Threat Vectors Proportion
            </h3>

            {/* Custom SVG Segment Circle simulating actual visual specs */}
            <div className="flex flex-col items-center justify-center py-4 select-none relative">
              <svg className="w-36 h-36 transform -rotate-90" viewBox="0 0 100 100">
                {/* Background tracks */}
                <circle cx="50" cy="50" r="40" fill="none" stroke="#131127" strokeWidth="8" />
                
                {/* Access Trigger (Teal) */}
                <circle
                  cx="50"
                  cy="50"
                  r="40"
                  fill="none"
                  stroke="#00f59b"
                  strokeWidth="8"
                  strokeDasharray={`${accessPct * 2.51} 251`}
                  strokeDashoffset={0}
                />
                
                {/* System Alarm (Yellow) */}
                <circle
                  cx="50"
                  cy="50"
                  r="40"
                  fill="none"
                  stroke="#f59e0b"
                  strokeWidth="8"
                  strokeDasharray={`${alarmPct * 2.51} 251`}
                  strokeDashoffset={`-${accessPct * 2.51}`}
                />

                {/* Core Threat Trigger (Red-coral) */}
                <circle
                  cx="50"
                  cy="50"
                  r="40"
                  fill="none"
                  stroke="#fc2e5c"
                  strokeWidth="8"
                  strokeDasharray={`${triggerPct * 2.51} 251`}
                  strokeDashoffset={`-${(accessPct + alarmPct) * 2.51}`}
                />
              </svg>

              {/* Absolute Centered Stat Display */}
              <div className="absolute flex flex-col items-center justify-center">
                <span className="font-sans font-bold text-2xl text-white">
                  {logs.length}
                </span>
                <span className="font-sans text-[10px] text-[#8e8a9f] font-semibold uppercase tracking-wider">
                  Traged Logs
                </span>
              </div>
            </div>

            {/* Colored Segment Guide Key */}
            <div className="flex flex-col gap-3.5 mt-4 border-t border-[#1e1c31] pt-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2.5">
                  <span className="w-2.5 h-2.5 rounded-full bg-[#fc2e5c] block" />
                  <span className="font-sans text-xs text-[#8e8a9f]">Critical Threat Points</span>
                </div>
                <span className="font-sans text-xs text-[#fc2e5c] font-black">{triggerPct}%</span>
              </div>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2.5">
                  <span className="w-2.5 h-2.5 rounded-full bg-[#f59e0b] block" />
                  <span className="font-sans text-xs text-[#8e8a9f]">Hardware Alarms</span>
                </div>
                <span className="font-sans text-xs text-[#f59e0b] font-black">{alarmPct}%</span>
              </div>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2.5">
                  <span className="w-2.5 h-2.5 rounded-full bg-[#00f59b] block" />
                  <span className="font-sans text-xs text-[#8e8a9f]">Authorized Sessions</span>
                </div>
                <span className="font-sans text-xs text-[#00f59b] font-black">{accessPct}%</span>
              </div>
            </div>

          </div>

          {/* Ticking Live Network Feed widget */}
          <div className="bg-[#0c0b18] border border-[#1e1c31] rounded-2xl p-6 shadow-md">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-sans font-bold text-sm text-white">
                Live Transaction Stream
              </h3>
              <span className="font-sans text-[10px] bg-[#00f59b]/10 border border-[#00f59b]/25 text-[#00f59b] px-2.5 py-0.5 rounded-xl uppercase font-bold animate-pulse">
                Active Feed
              </span>
            </div>

            {/* Ticker Stream Content */}
            <div className="flex flex-col gap-4 mt-5">
              {feed.slice(0, 5).map((f) => {
                const isBlock = f.type === "block";
                const isKey = f.type === "key_rotation";

                return (
                  <div key={f.id} className="flex gap-3 justify-start items-start text-left text-xs leading-normal relative group">
                    <div className="flex flex-col items-center shrink-0">
                      <span className={`w-2 h-2 rounded-full block border mt-1.5 ${
                        isBlock 
                          ? "bg-[#fc2e5c] border-transparent"
                          : isKey
                          ? "bg-[#6122e6] border-transparent"
                          : "bg-[#00f59b] border-transparent"
                      }`} />
                    </div>
                    <div className="flex flex-col min-w-0">
                      <p className="font-sans text-[#8e8a9f] leading-tight font-medium">
                        {f.text}
                      </p>
                      <span className="font-sans text-[10px] text-[#8e8a9f]/50 mt-1">
                        {f.timestamp}
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>
            
          </div>

        </div>

      </div>

    </div>
  );
}
