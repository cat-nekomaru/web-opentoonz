// -----------------------------------------------
// interaction.js — ドラッグ・ピンチ・ホイール操作
// -----------------------------------------------

export function setupInteraction(canvas, state, onUpdate) {

  // ---- マウス：ドラッグ ----------------------------------------
  let dragging = false;
  let lastX = 0, lastY = 0;

  canvas.addEventListener('mousedown', (e) => {
    dragging = true;
    lastX = e.clientX;
    lastY = e.clientY;
  });

  canvas.addEventListener('mousemove', (e) => {
    if (!dragging) return;
    const dx = e.clientX - lastX;
    const dy = e.clientY - lastY;
    lastX = e.clientX;
    lastY = e.clientY;

    // ピクセル → クリップ空間へ変換して加算
    state.tx += (dx / canvas.clientWidth)  * 2;
    state.ty -= (dy / canvas.clientHeight) * 2;  // Y軸反転
    onUpdate();
  });

  window.addEventListener('mouseup', () => { dragging = false; });

  // ---- マウス：ホイールズーム ----------------------------------
  canvas.addEventListener('wheel', (e) => {
    e.preventDefault();
    const factor = e.deltaY < 0 ? 1.1 : 0.9;
    zoomAround(e.clientX, e.clientY, factor, canvas, state);
    onUpdate();
  }, { passive: false });


  // ---- タッチ：ドラッグ & ピンチ ------------------------------
  let touches = [];
  let lastPinchDist = 0;

  canvas.addEventListener('touchstart', (e) => {
    e.preventDefault();
    touches = [...e.touches];
    if (touches.length === 1) {
      lastX = touches[0].clientX;
      lastY = touches[0].clientY;
    }
    if (touches.length === 2) {
      lastPinchDist = getPinchDist(touches);
    }
  }, { passive: false });

  canvas.addEventListener('touchmove', (e) => {
    e.preventDefault();
    touches = [...e.touches];

    // 1本指：ドラッグ
    if (touches.length === 1) {
      const dx = touches[0].clientX - lastX;
      const dy = touches[0].clientY - lastY;
      lastX = touches[0].clientX;
      lastY = touches[0].clientY;
      state.tx += (dx / canvas.clientWidth)  * 2;
      state.ty -= (dy / canvas.clientHeight) * 2;
      onUpdate();
    }

    // 2本指：ピンチズーム
    if (touches.length === 2) {
      const dist = getPinchDist(touches);
      const factor = dist / lastPinchDist;
      lastPinchDist = dist;

      // ピンチ中心点
      const cx = (touches[0].clientX + touches[1].clientX) / 2;
      const cy = (touches[0].clientY + touches[1].clientY) / 2;
      zoomAround(cx, cy, factor, canvas, state);
      onUpdate();
    }
  }, { passive: false });

  canvas.addEventListener('touchend', (e) => {
    touches = [...e.touches];
    if (touches.length === 1) {
      lastX = touches[0].clientX;
      lastY = touches[0].clientY;
    }
  });


  // ---- ヘルパー ------------------------------------------------

  // ピンチ距離
  function getPinchDist(t) {
    const dx = t[0].clientX - t[1].clientX;
    const dy = t[0].clientY - t[1].clientY;
    return Math.sqrt(dx * dx + dy * dy);
  }

  // 指定スクリーン座標を中心にズーム
  function zoomAround(screenX, screenY, factor, canvas, state) {
    // スクリーン座標 → クリップ空間
    const cx = (screenX / canvas.clientWidth)  * 2 - 1;
    const cy = -((screenY / canvas.clientHeight) * 2 - 1);

    // ズーム前の画像上の点が変わらないよう tx/ty を補正
    state.tx = cx + (state.tx - cx) * factor;
    state.ty = cy + (state.ty - cy) * factor;
    state.scale *= factor;

    // ズーム範囲を制限（0.1〜20倍）
    state.scale = Math.max(0.1, Math.min(20, state.scale));
  }
}
