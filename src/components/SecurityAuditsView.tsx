/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { useState, useEffect, useRef } from "react";
import { Brain, X, ArrowRight, Info, Zap, Sparkles, User, AppWindow } from "lucide-react";

interface SecurityAuditsViewProps {
  onClose: () => void;
  onRunAudit: (prompt: string) => Promise<string>;
}

interface ChatMessage {
  id: string;
  sender: 'user' | 'gemini';
  text: string;
  timestamp: string;
}

export default function SecurityAuditsView({
  onClose,
  onRunAudit
}: SecurityAuditsViewProps) {
  const [prompt, setPrompt] = useState("");
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: "initial-welcome",
      sender: "gemini",
      text: "# Decrypted AI Console Entry\nWelcome to the **SecOS AI Prompt Auditor**. I have direct access to our live system configurations, security policies, fleet checklists, threat signals, and enrolled administration operator logs.\n\nAsk me any question below or click a diagnostic template to evaluate and secure the network infrastructure.",
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    }
  ]);
  const [loading, setLoading] = useState(false);
  const [loadingStep, setLoadingStep] = useState(0);

  const chatEndRef = useRef<HTMLDivElement>(null);

  const templates = [
    { title: "Universal Security Audit", query: "Run a general, holistic security health assessment of the Guardian network." },
    { title: "Evaluate Failed Auth Bursts", query: "Evaluate threat potential of the latest 15 failed administrative logins burst from IP 192.168.1.104. Suggest mitigations." },
    { title: "Analyze OS Fleet Vulnerabilities", query: "We have John Doe and Maria Kyle secure on SecOS stable, but Alice Smith is inactive and Evan Roberts is blocked on an unknown Android 13 device. Review device hygiene policy state." }
  ];

  // Auto-scroll to the bottom of the chat view
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, loading]);

  const handleExecute = async (queryToRun: string) => {
    if (!queryToRun.trim() || loading) return;

    const timeString = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const userMessage: ChatMessage = {
      id: `user-${Date.now()}`,
      sender: 'user',
      text: queryToRun,
      timestamp: timeString
    };

    setMessages((prev) => [...prev, userMessage]);
    setPrompt("");
    setLoading(true);
    setLoadingStep(0);

    const interval = setInterval(() => {
      setLoadingStep((prev) => (prev < 3 ? prev + 1 : prev));
    }, 1200);

    try {
      const responseText = await onRunAudit(queryToRun);
      const geminiMessage: ChatMessage = {
        id: `gemini-${Date.now()}`,
        sender: 'gemini',
        text: responseText,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      };
      setMessages((prev) => [...prev, geminiMessage]);
    } catch (err) {
      const errorMessage: ChatMessage = {
        id: `gemini-error-${Date.now()}`,
        sender: 'gemini',
        text: "### Diagnostic Interrupted\nFailed to authenticate Gemini TLS handshakes. Verify active environment API credentials.",
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      clearInterval(interval);
      setLoading(false);
    }
  };

  const renderFormattedReport = (rawText: string) => {
    if (!rawText) return null;

    const lines = rawText.split("\n");
    return lines.map((line, idx) => {
      if (line.startsWith("# ")) {
        return (
          <h2 key={idx} className="font-sans font-extrabold text-[18px] text-[#00ff88] mt-4 mb-3 border-b border-[#2a3441] pb-1.5 tracking-tight flex items-center gap-2">
            <span>{line.substring(2)}</span>
          </h2>
        );
      }
      if (line.startsWith("## ")) {
        return (
          <h3 key={idx} className="font-sans font-bold text-sm text-teal-300 mt-4 mb-2">
            {line.substring(3)}
          </h3>
        );
      }
      if (line.startsWith("### ")) {
        return (
          <h4 key={idx} className="font-sans font-bold text-[11px] text-[#d9e3f5] mt-3.5 mb-1.5 uppercase tracking-wide">
            {line.substring(4)}
          </h4>
        );
      }
      if (line.startsWith("- ") || line.startsWith("* ")) {
        const cleaned = line.substring(2);
        return (
          <div key={idx} className="flex gap-2 items-start justify-start text-[11px] leading-relaxed text-[#b9cbb9] my-0.5 ml-3 text-left">
            <span className="text-[#00ff88] text-[13px] mt-0.5">•</span>
            <p className="flex-1">{parseBoldTokens(cleaned)}</p>
          </div>
        );
      }
      if (/^\d+\.\s/.test(line)) {
        const cleaned = line.replace(/^\d+\.\s/, "");
        return (
          <div key={idx} className="flex gap-2 items-start justify-start text-[11px] leading-relaxed text-[#b9cbb9] my-1 ml-3 text-left">
            <span className="font-mono text-[9px] text-[#00ff88] mt-0.5">{line.match(/^\d+/)?.toString()}.</span>
            <p className="flex-1">{parseBoldTokens(cleaned)}</p>
          </div>
        );
      }
      if (!line.trim()) {
        return <div key={idx} className="h-2" />;
      }
      return (
        <p key={idx} className="font-sans text-[11px] leading-relaxed text-[#b9cbb9] my-0.5 text-left">
          {parseBoldTokens(line)}
        </p>
      );
    });
  };

  const parseBoldTokens = (text: string) => {
    const parts = text.split(/(\*\*.*?\*\*|`.*?`)/g);
    return parts.map((part, i) => {
      if (part.startsWith("**") && part.endsWith("**")) {
        return <strong key={i} className="text-white font-bold">{part.slice(2, -2)}</strong>;
      }
      if (part.startsWith("`") && part.endsWith("`")) {
        return <code key={i} className="bg-[#050f1b] border border-[#232d3a] px-1 py-0.5 rounded text-[10px] font-mono text-teal-300 font-bold">{part.slice(1, -1)}</code>;
      }
      return part;
    });
  };

  const loadingStepsText = [
    "Establishing Secure Handshake with Gemini AI...",
    "Injecting real-time system database state parameters...",
    "Synthesizing threat signals through intelligence module...",
    "Generating cryptographic audit results..."
  ];

  return (
    <div className="fixed inset-y-0 right-0 z-50 w-full max-w-2xl bg-[#141b25]/95 border-l border-[#2a3441] shadow-2xl backdrop-blur-md flex flex-col justify-between animate-slide-in-right">
      
      {/* Header banner */}
      <div className="px-6 py-5 border-b border-[#2a3441] flex justify-between items-center bg-[#101720] shrink-0">
        <div className="flex items-center gap-3 select-none text-left">
          <div className="w-9 h-9 rounded-full bg-[#00ff88]/15 border border-[#3b4b3d] flex items-center justify-center text-[#00ff88]">
            <Brain className="w-5.5 h-5.5 animate-pulse" />
          </div>
          <div className="flex flex-col">
            <h3 className="font-sans font-extrabold text-sm text-[#d9e3f5]">
              Gemini AI Security Auditor
            </h3>
            <span className="font-mono text-[9px] text-[#00ff88] font-semibold uppercase tracking-widest">
              SecOS Cognitive Module
            </span>
          </div>
        </div>

        <button
          onClick={onClose}
          className="text-[#b9cbb9]/55 hover:text-white transition-colors border border-[#2a3441] hover:bg-[#2a3441] w-8 h-8 rounded-lg cursor-pointer flex justify-center items-center"
        >
          <X className="w-4.5 h-4.5" />
        </button>
      </div>

      {/* Primary Auditing interaction area / Chat Container */}
      <div className="flex-1 overflow-y-auto p-6 flex flex-col gap-6">
        
        {/* Quick Diagnostic Notice Banner */}
        <div className="bg-[#00a3ff]/10 border border-[#00a3ff]/30 rounded-xl px-4 py-3 flex gap-3 text-left items-start shrink-0 select-none">
          <Info className="text-blue-400 mt-0.5 shrink-0 w-4.5 h-4.5" />
          <div className="flex flex-col">
            <span className="font-sans font-bold text-xs text-blue-300">Live Secure Connection</span>
            <p className="font-sans text-[10.5px] text-[#b9cbb9]/70 leading-normal mt-0.5">
              I have dynamic direct access to configurations, event threat logs, user lists, and directories to answer any question about this site.
            </p>
          </div>
        </div>

        {/* Dynamic Interactive Chat History Timeline */}
        <div className="flex-1 flex flex-col gap-4">
          {messages.map((msg) => {
            const isUser = msg.sender === 'user';
            return (
              <div
                key={msg.id}
                className={`flex gap-3 max-w-[88%] ${isUser ? 'self-end flex-row-reverse text-right' : 'self-start text-left'}`}
              >
                {/* Visual Avatar Identifier */}
                <div className={`w-7 h-7 rounded-full flex items-center justify-center flex-shrink-0 select-none ${isUser ? 'bg-[#6122e6]/25 border border-[#6122e6]' : 'bg-[#00ff88]/15 border border-[#3b4b3d]'}`}>
                  {isUser ? (
                    <User className="w-3.5 h-3.5 text-[#fff]" />
                  ) : (
                    <Brain className="w-3.5 h-3.5 text-[#00ff88]" />
                  )}
                </div>

                <div className="flex flex-col gap-1 w-full">
                  {/* Bubble Content Area */}
                  <div className={`rounded-2xl p-4 shadow-sm select-text ${
                    isUser
                      ? 'bg-[#6122e6] text-white rounded-tr-none border border-[#763bf3]/35 text-xs text-left leading-relaxed font-sans'
                      : 'bg-[#101720]/80 text-[#b9cbb9] rounded-tl-none border border-[#2a3441] text-xs'
                  }`}>
                    {isUser ? (
                      <p className="whitespace-pre-wrap">{msg.text}</p>
                    ) : (
                      <div className="prose prose-invert prose-xs">
                        {renderFormattedReport(msg.text)}
                      </div>
                    )}
                  </div>
                  {/* Timestamp Label */}
                  <span className={`font-mono text-[8.5px] text-[#b9cbb9]/40 select-none ${isUser ? 'text-right' : 'text-left'}`}>
                    {msg.timestamp}
                  </span>
                </div>
              </div>
            );
          })}

          {/* Audit Loader Bubble Sequence */}
          {loading && (
            <div className="flex gap-3 max-w-[88%] self-start text-left animate-pulse">
              <div className="w-7 h-7 rounded-full bg-[#00ff88]/15 border border-[#3b4b3d] flex items-center justify-center flex-shrink-0">
                <Brain className="w-3.5 h-3.5 text-[#00ff88] animate-pulse" />
              </div>

              <div className="flex flex-col gap-1 w-full">
                <div className="bg-[#101720]/80 rounded-2xl p-4 rounded-tl-none border border-[#2a3441] text-xs">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="w-1.5 h-1.5 rounded-full bg-[#00ff88] animate-ping" />
                    <span className="font-mono text-[9.5px] text-[#00ff88] font-bold uppercase tracking-wider">
                      SecOS Quantum Core Thinking...
                    </span>
                  </div>
                  <p className="font-sans text-[10px] text-[#b9cbb9]/60 italic">
                    {loadingStepsText[loadingStep]}
                  </p>
                </div>
              </div>
            </div>
          )}

          {/* Dummy element for scroll targeting */}
          <div ref={chatEndRef} />
        </div>

        {/* Quick Audit Templates List */}
        {!loading && (
          <div className="flex flex-col gap-2 shrink-0 border-t border-[#2a3441]/40 pt-4 select-none">
            <span className="font-mono text-[9px] text-[#b9cbb9]/40 uppercase tracking-wider block text-left">
              Quick Diagnosis Suggestions
            </span>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
              {templates.map((tpl, i) => (
                <button
                  key={i}
                  type="button"
                  onClick={() => handleExecute(tpl.query)}
                  className="text-left p-2.5 rounded-lg border border-[#2a3441] bg-[#0a1420]/30 hover:border-[#00ff88]/45 hover:bg-[#0a1420]/50 transition-all font-sans text-[10.5px] cursor-pointer group flex flex-col gap-1 justify-between min-h-[58px]"
                >
                  <span className="font-bold text-[#d9e3f5] group-hover:text-[#00ff88] transition-colors line-clamp-1">
                    {tpl.title}
                  </span>
                  <span className="text-[#b9cbb9]/45 line-clamp-1 font-sans text-[9px]">{tpl.query}</span>
                </button>
              ))}
            </div>
          </div>
        )}

      </div>

      {/* Drawer bottom compose form inputs */}
      <div className="p-4 border-t border-[#2a3441] bg-[#101720] shrink-0">
        <form
          className="flex gap-2"
          onSubmit={(e) => {
            e.preventDefault();
            if (prompt.trim() && !loading) {
              handleExecute(prompt.trim());
            }
          }}
        >
          <input
            type="text"
            disabled={loading}
            className="flex-1 bg-[#050f1b] border border-[#2a3441] rounded-lg px-4 py-3 outline-none font-sans text-xs focus:border-[#00ff88]/50 text-white placeholder:text-[#b9cbb9]/30"
            placeholder={loading ? "AI is summarizing..." : "Ask customized security audit queries or site questions..."}
            value={prompt}
            onChange={(e) => setPrompt(e.target.value)}
          />
          <button
            type="submit"
            disabled={loading || !prompt.trim()}
            className="bg-[#00ff88] text-[#050f1b] font-mono font-extrabold px-4 rounded-lg hover:bg-[#60ff99] transition-all cursor-pointer flex items-center justify-center gap-1.5 disabled:opacity-40"
          >
            <Zap className="w-3.5 h-3.5" />
            <span className="text-xs">Send</span>
          </button>
        </form>
      </div>

    </div>
  );
}
