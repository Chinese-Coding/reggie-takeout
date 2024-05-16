(function (doc: Document, win: Window) {
    const docEl = doc.documentElement,
        resizeEvt: string = 'orientationchange' in window ? 'orientationchange' : 'resize',
        recalc = function (): void {
            const clientWidth: number = docEl.clientWidth;
            if (!clientWidth) return;
            if (clientWidth > 750) {
                docEl.style.fontSize = '28px';
            } else {
                docEl.style.fontSize = `${clientWidth / 375}px`;
            }
        };

    if (!doc.addEventListener) return;
    win.addEventListener(resizeEvt, recalc, false);
    doc.addEventListener('DOMContentLoaded', recalc, false);
})(document, window);
