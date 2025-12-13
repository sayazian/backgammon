const pipsByValue = {
    1:['p5'], 2:['p1','p9'], 3:['p1','p5','p9'],
    4:['p1','p3','p7','p9'], 5:['p1','p3','p5','p7','p9'],
    6:['p1','p3','p4','p6','p7','p9']
};

function renderDie(el, val){
    el.innerHTML='';
    (pipsByValue[val]||[]).forEach(cls=>{
        const pip=document.createElement('div'); pip.className='pip '+cls; el.appendChild(pip);
    });
}

function rollDice(){
    const d1=Math.floor(Math.random()*6)+1;
    const d2=Math.floor(Math.random()*6)+1;
    renderDie(die1,d1); renderDie(die2,d2);
    console.log('Rolled', d1, d2);
}

function buildBoard(){
    const halves=[document.getElementById('half-left'), document.getElementById('half-right')];
    [0,1].forEach((h,hi)=>{
        for(let i=0;i<12;i++){
            const point=document.createElement('div');
            let idx;
            if (hi === 0) { // left half
                idx = i < 6 ? 13 + i : 12 - (i - 6); // top: 13-18, bottom: 12-7
            } else { // right half
                idx = i < 6 ? 19 + i : 6 - (i - 6); // top: 19-24, bottom: 6-1
            }
            point.className='point '+(i<6?'top':'bottom')+(i%2?'':' alt');
            point.dataset.point=idx;
            point.onclick=()=>console.log('Clicked point', idx);
            const label=document.createElement('div');
            label.className='label';
            label.textContent=idx;
            point.appendChild(label);
            const pips=document.createElement('div'); pips.className='pips'; point.appendChild(pips);
            halves[hi].appendChild(point);
        }
    });
    // Outer rail click handlers
    document.querySelectorAll('.outer-rect').forEach(rect => {
        rect.onclick = () => {
            console.log('Clicked outer rail', rect.dataset.outer);
        };
    });
}

document.getElementById('roll').onclick=rollDice;
document.getElementById('die1').onclick=rollDice;
document.getElementById('die2').onclick=rollDice;

buildBoard();
rollDice(); // initial render
