import React, { useEffect, useRef } from 'react';

// Monochrome Palette matching Kotlin
const TRUNK_BASE      = '#404040';
const TRUNK_MAIN      = '#6A6A6A';
const TRUNK_MID       = '#8A8A8A';
const TRUNK_HIGHLIGHT = '#B0B0B0';

const SOIL_BASE       = '#1A1A1A';
const SOIL_MID        = '#2C2C2C';
const SOIL_SURFACE    = '#424242';
const SOIL_HIGHLIGHT  = '#6E6E6E';

const CANOPY_DARKEST  = '#2E2E2E';
const CANOPY_DARK     = '#555555';
const CANOPY_MID      = '#888888';
const CANOPY_LIGHT    = '#BBBBBB';
const CANOPY_HILIGHT  = '#DDDDDD';
const CANOPY_WHITE    = '#FFFFFF';

export default function TreeVisualizer({ stage = 'Tree', healthRatio = 1, showGlow = true, className = "" }) {
  const canvasRef = useRef(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    
    // Handle high DPI displays
    const dpr = window.devicePixelRatio || 1;
    const rect = canvas.getBoundingClientRect();
    canvas.width = rect.width * dpr;
    canvas.height = rect.height * dpr;
    ctx.scale(dpr, dpr);

    let animationFrameId;
    const startTime = Date.now();

    function rgba(r, g, b, a) {
      return `rgba(${r},${g},${b},${a})`;
    }

    function hexToRgba(hex, alpha) {
      let r = parseInt(hex.slice(1, 3), 16),
          g = parseInt(hex.slice(3, 5), 16),
          b = parseInt(hex.slice(5, 7), 16);
      return `rgba(${r},${g},${b},${alpha})`;
    }

    // Easings
    const fastOutSlowIn = t => t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;

    const render = () => {
      const now = Date.now();
      const elapsed = now - startTime;

      // Animations
      // sway: -1.2 to 1.2, 4500ms reverse
      const swayCycle = (elapsed % 9000) / 4500;
      const swayT = fastOutSlowIn(swayCycle > 1 ? 2 - swayCycle : swayCycle);
      const swayAngle = -1.2 + (2.4 * swayT);

      // glowPulse: 0.82 to 1.28, 2800ms reverse
      const glowCycle = (elapsed % 5600) / 2800;
      const glowT = fastOutSlowIn(glowCycle > 1 ? 2 - glowCycle : glowCycle);
      const glowPulse = 0.82 + (0.46 * glowT);

      // particleAngle: 0 to 360, 8000ms loop
      const particleAngle = ((elapsed % 8000) / 8000) * 360;

      // seedBreath: 0.94 to 1.06, 2000ms reverse
      const breathCycle = (elapsed % 4000) / 2000;
      const breathT = fastOutSlowIn(breathCycle > 1 ? 2 - breathCycle : breathCycle);
      const seedBreath = 0.94 + (0.12 * breathT);

      // Draw
      const w = rect.width;
      const h = rect.height;
      ctx.clearRect(0, 0, w, h);

      const rootX = w * 0.5;
      const hr = Math.max(0.5, Math.min(1.3, healthRatio));

      // Utils
      const drawCircle = (x, y, radius, fill, stroke, strokeW) => {
        ctx.beginPath();
        ctx.arc(x, y, radius, 0, Math.PI * 2);
        if (fill) { ctx.fillStyle = fill; ctx.fill(); }
        if (stroke) { ctx.lineWidth = strokeW; ctx.strokeStyle = stroke; ctx.stroke(); }
      };

      const drawFoliageSphere = (cx, cy, r) => {
        // Shadow
        ctx.beginPath();
        ctx.ellipse(cx - r * 0.9, cy + r * 0.7, r * 0.9, r * 0.2, 0, 0, Math.PI * 2);
        ctx.fillStyle = rgba(0,0,0,0.145);
        ctx.fill();

        drawCircle(cx, cy, r, CANOPY_DARKEST);

        const grad = ctx.createRadialGradient(cx + r * 0.2, cy + r * 0.2, 0, cx, cy, r);
        grad.addColorStop(0, rgba(0,0,0,0));
        grad.addColorStop(0.5, rgba(0,0,0,0.07));
        grad.addColorStop(1, rgba(0,0,0,0.25));
        drawCircle(cx, cy, r, grad);

        drawCircle(cx - r * 0.05, cy - r * 0.02, r * 0.88, CANOPY_DARK);
        drawCircle(cx - r * 0.08, cy - r * 0.06, r * 0.72, CANOPY_MID);

        // Veins
        const veins = [
          [[cx - r * 0.3, cy - r * 0.35], [cx + r * 0.25, cy + r * 0.15]],
          [[cx - r * 0.1, cy - r * 0.4],  [cx + r * 0.35, cy + r * 0.3]],
          [[cx - r * 0.4, cy - r * 0.1],  [cx + r * 0.1,  cy + r * 0.4]]
        ];
        ctx.lineCap = 'round';
        veins.forEach(([[x1,y1], [x2,y2]]) => {
          ctx.beginPath();
          ctx.moveTo(x1, y1);
          ctx.quadraticCurveTo(cx, cy, x2, y2);
          ctx.strokeStyle = rgba(0,0,0,0.06);
          ctx.lineWidth = r * 0.035;
          ctx.stroke();
          ctx.strokeStyle = rgba(255,255,255,0.03);
          ctx.lineWidth = r * 0.02;
          ctx.stroke();
        });

        drawCircle(cx - r * 0.22, cy - r * 0.26, r * 0.38, CANOPY_LIGHT);
        drawCircle(cx - r * 0.28, cy - r * 0.32, r * 0.20, CANOPY_HILIGHT);
        drawCircle(cx - r * 0.32, cy - r * 0.38, r * 0.09, rgba(255,255,255,0.8));
        
        drawCircle(cx, cy, r, null, CANOPY_DARK, r * 0.04);
      };

      const drawRichTrunk = (baseX, baseY, topX, topY, baseW, topW) => {
        ctx.fillStyle = rgba(0,0,0,0.37);
        ctx.beginPath();
        ctx.moveTo(baseX - baseW - 3, baseY + 4);
        ctx.bezierCurveTo(baseX - baseW * 0.9, baseY - (baseY - topY) * 0.4, topX - topW * 1.3, topY + (baseY - topY) * 0.28, topX - topW - 2, topY + 2);
        ctx.lineTo(topX + topW + 2, topY + 2);
        ctx.bezierCurveTo(topX + topW * 1.3, topY + (baseY - topY) * 0.28, baseX + baseW * 0.9, baseY - (baseY - topY) * 0.4, baseX + baseW + 3, baseY + 4);
        ctx.fill();

        ctx.fillStyle = TRUNK_BASE;
        ctx.beginPath();
        ctx.moveTo(baseX - baseW, baseY);
        ctx.bezierCurveTo(baseX - baseW * 0.82, baseY - (baseY - topY) * 0.38, topX - topW * 1.15, topY + (baseY - topY) * 0.3, topX - topW, topY);
        ctx.lineTo(topX + topW, topY);
        ctx.bezierCurveTo(topX + topW * 1.15, topY + (baseY - topY) * 0.3, baseX + baseW * 0.82, baseY - (baseY - topY) * 0.38, baseX + baseW, baseY);
        ctx.fill();

        ctx.fillStyle = TRUNK_MAIN;
        ctx.beginPath();
        ctx.moveTo(baseX - baseW * 0.55, baseY);
        ctx.bezierCurveTo(baseX - baseW * 0.42, baseY - (baseY - topY) * 0.38, topX - topW * 0.55, topY + (baseY - topY) * 0.3, topX - topW * 0.4, topY);
        ctx.lineTo(topX + topW * 0.4, topY);
        ctx.bezierCurveTo(topX + topW * 0.55, topY + (baseY - topY) * 0.3, baseX + baseW * 0.42, baseY - (baseY - topY) * 0.38, baseX + baseW * 0.55, baseY);
        ctx.fill();

        ctx.lineCap = 'round';
        ctx.beginPath();
        ctx.moveTo(baseX - baseW * 0.12, baseY);
        ctx.bezierCurveTo(baseX - baseW * 0.08, baseY - (baseY - topY) * 0.38, topX - topW * 0.15, topY + (baseY - topY) * 0.3, topX, topY);
        ctx.lineWidth = baseW * 0.28;
        ctx.strokeStyle = TRUNK_MID;
        ctx.stroke();
        ctx.lineWidth = baseW * 0.10;
        ctx.strokeStyle = TRUNK_HIGHLIGHT;
        ctx.stroke();
      };

      const drawRootFlares = (x, y, s) => {
        const roots = [ [-28, 4, -52, 14], [28, 4, 50, 13], [-14, 2, -34, 20], [14, 2, 32, 20] ];
        roots.forEach(([ox, oy, ex, ey]) => {
          ctx.beginPath();
          ctx.moveTo(x + ox * s, y + oy * s);
          ctx.bezierCurveTo(x + ox * s * 0.6, y + oy * s + 6 * s, x + ex * s * 0.7, y + ey * s * 0.7, x + ex * s, y + ey * s);
          const w = Math.max(3, 6 - Math.abs(ox) * 0.06) * s;
          ctx.lineWidth = w; ctx.strokeStyle = TRUNK_BASE; ctx.stroke();
          ctx.lineWidth = w * 0.5; ctx.strokeStyle = TRUNK_MAIN; ctx.stroke();
        });
      };

      const drawBranch = (fx, fy, tx, ty, width) => {
        ctx.lineCap = 'round';
        ctx.beginPath(); ctx.moveTo(fx, fy); ctx.lineTo(tx, ty);
        ctx.lineWidth = width * 1.1; ctx.strokeStyle = TRUNK_BASE; ctx.stroke();
        ctx.lineWidth = width * 0.8; ctx.strokeStyle = TRUNK_MAIN; ctx.stroke();
        ctx.lineWidth = width * 0.3; ctx.strokeStyle = TRUNK_MID; ctx.stroke();
      };

      const drawLeaf = (x, y, angleDeg, size, color) => {
        ctx.fillStyle = color;
        ctx.beginPath();
        ctx.moveTo(x, y);
        ctx.bezierCurveTo(x - size*0.5, y - size*0.7, x - size*0.2, y - size, x, y - size*1.2);
        ctx.bezierCurveTo(x + size*0.2, y - size, x + size*0.5, y - size*0.7, x, y);
        ctx.fill();
      };

      const drawGlowOrb = (cx, cy, baseRadius, pulse, pAngle, pCount = 6) => {
        const r = baseRadius * pulse;
        drawCircle(cx, cy, r * 2.6, rgba(255,255,255,0.015));
        drawCircle(cx, cy, r * 2.0, rgba(255,255,255,0.023));
        drawCircle(cx, cy, r * 1.5, rgba(255,255,255,0.035));
        drawCircle(cx, cy, r * 1.15, rgba(255,255,255,0.055));
        drawCircle(cx, cy, r * 0.72, rgba(255,255,255,0.086));
        drawCircle(cx, cy, r * 0.38, rgba(255,255,255,0.13));

        for (let i = 0; i < pCount; i++) {
          const angle = (pAngle + (360 / pCount) * i) * (Math.PI / 180);
          const orbitR = r * 1.5;
          const px = cx + orbitR * Math.cos(angle);
          const py = cy + orbitR * Math.sin(angle);
          const pSize = r * 0.055 * (0.6 + 0.4 * ((i % 3) / 2));
          drawCircle(px, py, pSize, rgba(255,255,255,0.12));
          drawCircle(px, py, pSize * 1.5, rgba(255,255,255,0.06));
        }
      };

      const drawGroundMound = (x, y, radius, s) => {
        const grad = ctx.createRadialGradient(x, y + 4 * s, 0, x, y + 4 * s, radius * 1.4);
        grad.addColorStop(0, rgba(255,255,255,0.12));
        grad.addColorStop(1, rgba(0,0,0,0));
        ctx.beginPath();
        ctx.ellipse(x, y + 4*s, radius * 1.4, 16 * s, 0, 0, Math.PI * 2);
        ctx.fillStyle = grad;
        ctx.fill();

        ctx.beginPath();
        ctx.moveTo(x - radius, y + 6 * s);
        ctx.bezierCurveTo(x - radius * 0.6, y - 10 * s, x + radius * 0.6, y - 10 * s, x + radius, y + 6 * s);
        ctx.bezierCurveTo(x + radius * 0.5, y + 16 * s, x - radius * 0.5, y + 16 * s, x - radius, y + 6 * s);
        ctx.fillStyle = SOIL_BASE;
        ctx.fill();
        ctx.lineWidth = 1.8 * s;
        ctx.strokeStyle = SOIL_SURFACE;
        ctx.stroke();

        [[-0.45, 2, 2.8], [0.35, 1, 3.4], [-0.12, 5, 2.0], [0.6, 4, 2.3]].forEach(([ox, oy, r]) => {
          drawCircle(x + radius * ox, y + oy * s, r * s, SOIL_HIGHLIGHT);
        });
      };

      const drawDetailedTree = (x, y, sway, s, glow, pAngle, extraScale, showG) => {
        const sc = s * extraScale;
        const swayX = x + sway * 3.5;
        const trunkTopY = y - 160 * sc;

        if (showG) drawGlowOrb(swayX, trunkTopY - 38*sc, 88*sc, glow, pAngle, 7);
        drawRootFlares(x, y, sc);
        drawRichTrunk(x, y, swayX, trunkTopY, 24*sc, 11*sc);

        const midY = y - 72*sc;
        const midX = x + (swayX - x) * 0.45;
        const upperY = y - 118*sc;
        const upperX = x + (swayX - x) * 0.7;

        drawBranch(midX, midY, swayX - 72*sc, trunkTopY + 45*sc, 12*sc);
        drawBranch(midX, midY, swayX + 70*sc, trunkTopY + 40*sc, 11*sc);
        drawBranch(upperX, upperY, swayX - 52*sc, trunkTopY + 8*sc,  8.5*sc);
        drawBranch(upperX, upperY, swayX + 50*sc, trunkTopY + 2*sc,  8*sc);
        drawBranch(upperX, upperY, swayX, trunkTopY - 18*sc, 9*sc);

        drawFoliageSphere(swayX - 84*sc, trunkTopY + 30*sc, 40*sc);
        drawFoliageSphere(swayX + 82*sc, trunkTopY + 24*sc, 38*sc);
        drawFoliageSphere(swayX - 46*sc, trunkTopY - 2*sc, 46*sc);
        drawFoliageSphere(swayX + 44*sc, trunkTopY - 8*sc, 44*sc);
        drawFoliageSphere(swayX, trunkTopY - 36*sc, 58*sc);
      };

      const drawDetailedPlant = (x, y, sway, s, glow, pAngle, showG) => {
        const sc = s * 1.22;
        const trunkTopY = y - 130*sc;
        const swayX = x + sway * 3;
        if (showG) drawGlowOrb(swayX, trunkTopY - 30*sc, 58*sc, glow, pAngle, 5);
        drawRichTrunk(x, y, swayX, trunkTopY, 14*sc, 7*sc);
        const midY = y - 60*sc;
        const midX = x + (swayX - x) * 0.5;
        drawBranch(midX, midY, swayX - 46*sc, trunkTopY + 30*sc, 7*sc);
        drawBranch(midX, midY, swayX + 44*sc, trunkTopY + 25*sc, 6.5*sc);
        drawBranch(midX, midY, swayX, trunkTopY - 10*sc, 8*sc);

        drawFoliageSphere(swayX - 46*sc, trunkTopY + 14*sc, 34*sc);
        drawFoliageSphere(swayX + 44*sc, trunkTopY + 10*sc, 32*sc);
        drawFoliageSphere(swayX, trunkTopY - 30*sc, 42*sc);
      };

      const drawDetailedSprout = (x, y, sway, s, glow, pAngle, showG) => {
        const sc = s * 1.4;
        const tipX = x + sway * 2;
        const tipY = y - 90 * sc;
        if (showG) drawGlowOrb(tipX, tipY - 10*sc, 38*sc, glow, pAngle, 4);

        ctx.beginPath();
        ctx.moveTo(x, y);
        ctx.bezierCurveTo(x - 8*sc, y - 30*sc, tipX - 4*sc, y - 60*sc, tipX, tipY);
        ctx.lineCap = 'round';
        ctx.lineWidth = 7*sc; ctx.strokeStyle = TRUNK_BASE; ctx.stroke();
        ctx.lineWidth = 5*sc; ctx.strokeStyle = TRUNK_MAIN; ctx.stroke();
        ctx.lineWidth = 2*sc; ctx.strokeStyle = TRUNK_HIGHLIGHT; ctx.stroke();

        drawLeaf(x - 22*sc, y - 45*sc, -50, 22*sc, CANOPY_MID);
        drawLeaf(x + 24*sc, y - 55*sc, 45, 24*sc, CANOPY_LIGHT);
        drawFoliageSphere(tipX - 11*sc, tipY + 8*sc, 26*sc);
        drawFoliageSphere(tipX + 9*sc, tipY + 4*sc, 24*sc);
        drawFoliageSphere(tipX, tipY - 12*sc, 28*sc);
      };

      const drawDetailedSeed = (x, y, s, glow, breath, pAngle, showG) => {
        const soilW = 140 * s;
        const seedScale = s * 1.5 * breath;
        const seedX = x;
        const seedY = y - 4 * s;

        if (showG) drawGlowOrb(seedX, seedY - 10*s, 50*s, glow, pAngle, 5);

        ctx.beginPath();
        ctx.ellipse(x, y + 8*s, soilW * 1.05, soilW * 0.19, 0, 0, Math.PI*2);
        ctx.fillStyle = rgba(0,0,0,0.31);
        ctx.fill();

        ctx.beginPath();
        ctx.moveTo(x - soilW, y + 12*s);
        ctx.bezierCurveTo(x - soilW * 0.6, y - 14*s, x + soilW * 0.6, y - 14*s, x + soilW, y + 12*s);
        ctx.bezierCurveTo(x + soilW * 0.7, y + 26*s, x - soilW * 0.7, y + 26*s, x - soilW, y + 12*s);
        ctx.fillStyle = SOIL_MID; ctx.fill();

        ctx.beginPath();
        ctx.moveTo(x - soilW * 0.95, y + 10*s);
        ctx.bezierCurveTo(x - soilW * 0.55, y - 12*s, x + soilW * 0.55, y - 12*s, x + soilW * 0.95, y + 10*s);
        ctx.lineWidth = 2.2*s; ctx.strokeStyle = SOIL_HIGHLIGHT; ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(seedX, seedY - 18*seedScale);
        ctx.bezierCurveTo(seedX+16*seedScale, seedY-14*seedScale, seedX+16*seedScale, seedY+12*seedScale, seedX, seedY+20*seedScale);
        ctx.bezierCurveTo(seedX-16*seedScale, seedY+12*seedScale, seedX-16*seedScale, seedY-14*seedScale, seedX, seedY-18*seedScale);
        ctx.fillStyle = TRUNK_MAIN; ctx.fill();

        ctx.beginPath();
        ctx.moveTo(seedX, seedY - 12*seedScale);
        ctx.bezierCurveTo(seedX+9*seedScale, seedY-8*seedScale, seedX+9*seedScale, seedY+8*seedScale, seedX, seedY+13*seedScale);
        ctx.bezierCurveTo(seedX-9*seedScale, seedY+8*seedScale, seedX-9*seedScale, seedY-8*seedScale, seedX, seedY-12*seedScale);
        ctx.fillStyle = CANOPY_WHITE; ctx.fill();
        
        ctx.beginPath();
        const tipY = seedY - 45*seedScale;
        ctx.moveTo(seedX, seedY - 14*seedScale);
        ctx.bezierCurveTo(seedX - 4*seedScale, seedY - 26*seedScale, seedX + 6*seedScale, seedY - 36*seedScale, seedX + 2*seedScale, tipY);
        ctx.lineWidth = 5*seedScale; ctx.strokeStyle = TRUNK_MID; ctx.stroke();
        ctx.lineWidth = 2*seedScale; ctx.strokeStyle = CANOPY_WHITE; ctx.stroke();
        
        drawLeaf(seedX - 10*seedScale, tipY + 4*seedScale, -40, 18*seedScale, CANOPY_HILIGHT);
        drawLeaf(seedX + 12*seedScale, tipY + 2*seedScale, 38, 20*seedScale, CANOPY_WHITE);
      };

      if (stage === "Seed") {
        const seedY = h * 0.62;
        const baseDim = Math.min(w * 0.85, h * 0.7);
        const s = (baseDim / 240) * hr;
        drawDetailedSeed(rootX, seedY, s, glowPulse, seedBreath, particleAngle, showGlow);
      } else {
        const rootY = h * 0.84;
        const baseDim = Math.min(w * 0.85, h * 0.75);
        const s = (baseDim / 260) * hr;
        drawGroundMound(rootX, rootY, 110 * s, s);

        if (stage === "Sprout") drawDetailedSprout(rootX, rootY, swayAngle, s, glowPulse, particleAngle, showGlow);
        else if (stage === "Plant") drawDetailedPlant(rootX, rootY, swayAngle, s, glowPulse, particleAngle, showGlow);
        else if (stage === "Garden") {
          drawDetailedPlant(rootX - 88*s, rootY + 5, swayAngle * 0.7, s * 0.64, glowPulse, particleAngle, showGlow);
          drawDetailedSprout(rootX + 90*s, rootY + 8, swayAngle * -0.6, s * 0.68, glowPulse, particleAngle, showGlow);
          drawDetailedTree(rootX, rootY, swayAngle, s, glowPulse, particleAngle, 1.05, showGlow);
        }
        else if (stage === "Forest") {
          drawDetailedPlant(rootX - 108*s, rootY + 5, swayAngle * 0.5, s * 0.54, glowPulse, particleAngle, showGlow);
          drawDetailedPlant(rootX + 112*s, rootY + 6, swayAngle * -0.7, s * 0.56, glowPulse, particleAngle, showGlow);
          drawDetailedTree(rootX, rootY, swayAngle, s, glowPulse, particleAngle, 1.15, showGlow);
        }
        else drawDetailedTree(rootX, rootY, swayAngle, s, glowPulse, particleAngle, 1, showGlow);
      }

      animationFrameId = requestAnimationFrame(render);
    };

    render();

    return () => cancelAnimationFrame(animationFrameId);
  }, [stage, healthRatio, showGlow]);

  return <canvas ref={canvasRef} className={`w-full h-full ${className}`} />;
}
