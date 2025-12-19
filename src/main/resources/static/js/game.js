const pipsByValue = {
    1: ['p5'],
    2: ['p1', 'p9'],
    3: ['p1', 'p5', 'p9'],
    4: ['p1', 'p3', 'p7', 'p9'],
    5: ['p1', 'p3', 'p5', 'p7', 'p9'],
    6: ['p1', 'p3', 'p4', 'p6', 'p7', 'p9']
};

const die1 = document.getElementById('die1');
const die2 = document.getElementById('die2');
const hitCounts = {white: 0, black: 0};
const outCounts = {white: 0, black: 0};

// Safely read injected boardStatus (from Thymeleaf) if present
const injectedBoard = (typeof window.boardStatus === 'object' && window.boardStatus) ? window.boardStatus : null;
let boardState = normalizeBoardStatus(injectedBoard);
let currentTurn = boardState.turn || 'black';
let selectedPoint = null;
let selectedFromHit = false;

function normalizeBoardStatus(status) {
    if (!status || !Array.isArray(status.points)) {
        // Empty board fallback; server should inject the real starting layout
        const points = Array.from({ length: 24 }, (_v, i) => ({ position: i + 1, color: null, number: 0 }));
        return { points, hits: { white: 0, black: 0 }, outs: { white: 0, black: 0 }, dice: [0, 0], turn: 'black' };
    }
    // Shallow clone so we can mutate boardState without touching the injected object
    return {
        points: status.points.map((pt, idx) => ({
            position: Number(pt.position) || (idx + 1),
            color: pt.color,
            number: Number(pt.number) || 0
        })),
        hits: { ...(status.hits || {}) },
        outs: { ...(status.outs || {}) },
        dice: Array.isArray(status.dice) ? [...status.dice] : [0, 0],
        turn: status.turn || 'black'
    };
}

function renderDie(el, val) {
    el.innerHTML = '';
    (pipsByValue[val] || []).forEach(cls => {
        const pip = document.createElement('div');
        pip.className = 'pip ' + cls;
        el.appendChild(pip);
    });
}

function rollDice() {
    die1.classList.add('rolling');
    die2.classList.add('rolling');

    setTimeout(() => {
        const d1 = Math.floor(Math.random() * 6) + 1;
        const d2 = Math.floor(Math.random() * 6) + 1;
        boardState.dice = [d1, d2];

        // Switch turns and display
        currentTurn = currentTurn === 'white' ? 'black' : 'white';
        boardState.turn = currentTurn;
        renderDie(die1, d1);
        renderDie(die2, d2);
        updateTurn(currentTurn);
        clearSelection();

        die1.classList.remove('rolling');
        die2.classList.remove('rolling');

        console.log('Rolled', d1, d2);
    }, 400);
    // update Turn;
    // sendBoardStatus();
}

function clearBoardPoints() {
    document.querySelectorAll('.pips').forEach(p => p.innerHTML = '');
}

function renderCheckers(pointNum, color, count) {
    const point = document.querySelector(`[data-point="${pointNum}"]`);
    if (!point || !count) return;
    const pipsContainer = point.querySelector('.pips');
    for (let i = 0; i < count; i++) {
        const checker = document.createElement('div');
        checker.className = `checker ${color}`;
        pipsContainer.appendChild(checker);
    }
}

function clearSelection() {
    selectedPoint = null;
    selectedFromHit = false;
    document.querySelectorAll('.point.selected').forEach(p => p.classList.remove('selected'));
    document.querySelectorAll('[data-hit].selected').forEach(p => p.classList.remove('selected'));
}

function handlePointClick(pointNum) {
    const targetPointNum = Number(pointNum);
    console.log('Clicked point', pointNum);

    if (selectedFromHit) {
        tryEnterFromHit(targetPointNum);
        return;
    }

    if (selectedPoint && boardState.hits[currentTurn] === 0 && targetPointNum === 0) {
        tryBearOff(selectedPoint);
        return;
    }

    // If there are hits for the current player, they must re-enter first
    if (boardState.hits[currentTurn] > 0 && !selectedFromHit) {
        // If user clicks a point while having hits, treat it as an attempt to re-enter
        tryEnterFromHit(targetPointNum);
        return;
    }

    const pointData = boardState.points.find(p => p.position === targetPointNum);
    if (!pointData) return;

    if (!selectedPoint) {
        // Start selection if this point has current player's checkers
        if (pointData.color === currentTurn && pointData.number > 0) {
            selectedPoint = targetPointNum;
            console.log('Selected from point', pointNum);
            const el = document.querySelector(`[data-point="${pointNum}"]`);
            if (el) el.classList.add('selected');
        }
        return;
    }

    // Attempt move to destination
    console.log('Attempt move to point', pointNum);
    tryMoveChecker(selectedPoint, targetPointNum);
    clearSelection();
}

function handleHitClick(color) {
    if (color !== currentTurn) return;
    if (!boardState.hits[color] || boardState.hits[color] <= 0) return;
    clearSelection();
    selectedFromHit = true;
    selectedPoint = null;
    const el = document.querySelector(`[data-hit="${color}"]`);
    if (el) el.classList.add('selected');
}

function isEntryPoint(color, pointNum) {
    if (color === 'black') return pointNum >= 1 && pointNum <= 6;
    if (color === 'white') return pointNum >= 19 && pointNum <= 24;
    return false;
}

function tryMoveChecker(fromPointNum, toPointNum) {
    if (fromPointNum === toPointNum) return;
    const from = boardState.points.find(p => p.position === Number(fromPointNum));
    const to = boardState.points.find(p => p.position === Number(toPointNum));
    if (!from || !to) return;
    if (from.color !== currentTurn || from.number <= 0) {
        console.log('Move blocked: no checkers for current turn on point', fromPointNum);
        return;
    }

    const opponent = currentTurn === 'white' ? 'black' : 'white';
    const canHit = to.number === 1 && to.color && to.color !== currentTurn;

    // Allow move onto empty/same-color or hit a single opponent checker
    if (to.number > 0 && to.color !== currentTurn && !canHit) {
        console.log('Move blocked: destination occupied by opponent', toPointNum);
        return;
    }

    from.number -= 1;
    if (from.number === 0) {
        from.color = null;
    }

    if (canHit) {
        const hitColor = to.color;
        boardState.hits[hitColor] = (boardState.hits[hitColor] || 0) + 1;
        to.number = 1;
        to.color = currentTurn;
    } else {
        to.number += 1;
        to.color = currentTurn;
    }

    renderBoardFromStatus(boardState);
}

function tryEnterFromHit(toPointNum) {
    const target = Number(toPointNum);
    if (!isEntryPoint(currentTurn, target)) return;
    const to = boardState.points.find(p => p.position === target);
    if (!to) return;

    const opponent = currentTurn === 'white' ? 'black' : 'white';
    const canHit = to.number === 1 && to.color && to.color !== currentTurn;

    // Blocked if opponent has 2+ on the point
    if (to.number >= 2 && to.color === opponent) return;

    boardState.hits[currentTurn] = Math.max((boardState.hits[currentTurn] || 0) - 1, 0);

    if (canHit) {
        boardState.hits[to.color] = (boardState.hits[to.color] || 0) + 1;
        to.number = 1;
        to.color = currentTurn;
    } else {
        to.number += 1;
        to.color = currentTurn;
    }

    renderBoardFromStatus(boardState);
    clearSelection();
    console.log('Entered from hit to point', toPointNum);
}

function tryBearOff(fromPointNum) {
    const from = boardState.points.find(p => p.position === Number(fromPointNum));
    if (!from || from.number <= 0 || from.color !== currentTurn) {
        console.log('Bear off blocked: no checker to bear off from', fromPointNum);
        return;
    }
    from.number -= 1;
    if (from.number === 0) from.color = null;
    boardState.outs[currentTurn] = (boardState.outs[currentTurn] || 0) + 1;
    renderBoardFromStatus(boardState);
    clearSelection();
    console.log('Borne off from point', fromPointNum);
}

function buildBoard() {
    const halves = [document.getElementById('half-left'), document.getElementById('half-right')];

    [0, 1].forEach((h, hi) => {
        for (let i = 0; i < 12; i++) {
            const point = document.createElement('div');
            let idx;
            if (hi === 0) {
                idx = i < 6 ? 13 + i : 12 - (i - 6);
            } else {
                idx = i < 6 ? 19 + i : 6 - (i - 6);
            }
            point.className = 'point ' + (i < 6 ? 'top' : 'bottom') + (i % 2 ? '' : ' alt');
            point.dataset.point = idx;
            point.onclick = () => handlePointClick(idx);

            const label = document.createElement('div');
            label.className = 'label';
            label.textContent = idx;
            point.appendChild(label);

            const pips = document.createElement('div');
            pips.className = 'pips';
            point.appendChild(pips);

            halves[hi].appendChild(point);
        }
    });

    document.querySelectorAll('.outer-rect').forEach(rect => {
        const outColor = rect.querySelector('[data-out]')?.dataset.out;
        rect.onclick = () => {
            if (selectedPoint && outColor === currentTurn) {
                tryBearOff(selectedPoint);
            }
        };
    });

    document.querySelectorAll('[data-hit]').forEach(div => {
        const color = div.dataset.hit;
        div.onclick = () => handleHitClick(color);
    });
}


function renderStack(selector, color, count) {
    const el = document.querySelector(selector);
    if(!el) return;
    el.innerHTML = '';
    for (let i = 0; i < count; i++) {
        const checker = document.createElement('div');
        checker.className = `checker ${color}`;
        checker.style.setProperty('--stack-index', i);
        el.appendChild(checker);
    }
}

function setHit (color, count) {
    hitCounts[color] = count;
    renderStack(`[data-hit="${color}"]`,color, count);
}

function setOut (color, count) {
    outCounts[color] = count;
    renderStack(`[data-out="${color}"]`,color, count);
}

function updateTurn(turn) {
    const turnEl = document.getElementById('turn');
    if (!turnEl) return;
    const normalized = turn ? turn.charAt(0).toUpperCase() + turn.slice(1) : 'Unknown';
    turnEl.textContent = `Turn: ${normalized}`;
}

function renderBoardFromStatus(status) {
    if (!status) {
        clearBoardPoints();
        setHit('white', 0);
        setHit('black', 0);
        setOut('white', 0);
        setOut('black', 0);
        updateTurn(currentTurn);
        return;
    }

    clearBoardPoints();

    const points = Array.isArray(status.points) ? status.points : [];
    points.forEach((pt, idx) => {
        const pointNum = pt.position || idx + 1;
        if (!pt.color || !pt.number) return;
        renderCheckers(pointNum, pt.color, pt.number);
    });

    const hits = status.hits || {};
    setHit('white', hits.white ?? 0);
    setHit('black', hits.black ?? 0);

    const outs = status.outs || {};
    setOut('white', outs.white ?? 0);
    setOut('black', outs.black ?? 0);

    updateTurn(status.turn || currentTurn);

    if (Array.isArray(status.dice) && status.dice.length >= 2 && status.dice[0] > 0 && status.dice[1] > 0) {
        renderDie(die1, status.dice[0]);
        renderDie(die2, status.dice[1]);
    }
}

document.getElementById('roll').onclick = rollDice;
die1.onclick = rollDice;
die2.onclick = rollDice;

buildBoard();
renderBoardFromStatus(boardState);
if (!boardState || !boardState.dice || boardState.dice.length < 2 || boardState.dice[0] === 0 && boardState.dice[1] === 0) {
    rollDice();
}
