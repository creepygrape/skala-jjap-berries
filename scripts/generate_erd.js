const fs = require('fs');

const output = 'artifacts/berries-erd.svg';
const colors = {
  auth: ['#7c3aed', '#ede9fe'],
  channel: ['#0f766e', '#ccfbf1'],
  community: ['#2563eb', '#dbeafe'],
  commerce: ['#c2410c', '#ffedd5'],
  concert: ['#be123c', '#ffe4e6'],
};

const tables = [
  {name:'refresh_token', group:'auth', x:30,y:125, fields:[['PK','id','BIGINT'],['FK','user_id UNIQUE','BIGINT'],['','token UNIQUE','VARCHAR(512)'],['','expires_at','TIMESTAMP']]},
  {name:'revoked_access_token', group:'auth', x:30,y:290, fields:[['PK','id','BIGINT'],['','token_hash UNIQUE','CHAR(64)'],['','expires_at','TIMESTAMP']]},
  {name:'users', group:'auth', x:30,y:450, fields:[['PK','id','BIGINT'],['','email UNIQUE','VARCHAR(100)'],['','password','VARCHAR'],['','nickname UNIQUE','VARCHAR(30)'],['','profile_image_url','VARCHAR(500)'],['','role','USER|ARTIST|MANAGER'],['','status','ACTIVE|INACTIVE|WITHDRAWN'],['','deleted_at','TIMESTAMP NULL'],['','created_at / updated_at','TIMESTAMP']]},
  {name:'channel', group:'channel', x:370,y:125, fields:[['PK','id','BIGINT'],['','name UNIQUE','VARCHAR(100)'],['','description','VARCHAR(2000)'],['','profile_image_url','VARCHAR(500)']]},
  {name:'channel_user', group:'channel', x:370,y:315, fields:[['PK','id','BIGINT'],['FK','channel_id','BIGINT'],['FK','user_id UNIQUE','BIGINT'],['','created_at / updated_at','TIMESTAMP']]},
  {name:'fan_membership', group:'channel', x:370,y:510, fields:[['PK','id','BIGINT'],['FK','channel_id','BIGINT'],['FK','user_id','BIGINT'],['UQ','channel_id + user_id','COMPOSITE']]},
  {name:'post', group:'community', x:710,y:125, fields:[['PK','id','BIGINT'],['FK','channel_id','BIGINT'],['FK','author_id','BIGINT NULL'],['','title','VARCHAR(200)'],['','content','VARCHAR(10000)'],['','type','FAN|ARTIST|NOTICE'],['','deleted_at','TIMESTAMP NULL']]},
  {name:'comment', group:'community', x:1050,y:125, fields:[['PK','id','BIGINT'],['FK','post_id','BIGINT'],['FK','author_id','BIGINT NULL'],['FK','root_comment_id','BIGINT NULL'],['FK','reply_to_comment_id','BIGINT NULL'],['','content','VARCHAR(2000)'],['','deleted_at','TIMESTAMP NULL']]},
  {name:'post_like', group:'community', x:1390,y:125, fields:[['PK','id','BIGINT'],['FK','post_id','BIGINT'],['FK','user_id','BIGINT'],['UQ','post_id + user_id','COMPOSITE']]},
  {name:'product', group:'commerce', x:710,y:500, fields:[['PK','id','BIGINT'],['FK','channel_id','BIGINT'],['','name','VARCHAR(200)'],['','description','VARCHAR(2000)'],['','price','DECIMAL(19,0)'],['','stock','INTEGER'],['','status','ON_SALE|STOPPED']]},
  {name:'orders', group:'commerce', x:1050,y:500, fields:[['PK','id','BIGINT'],['FK','user_id','BIGINT'],['','status','PENDING|CANCELLED'],['','total_price','DECIMAL(19,0)'],['','created_at / updated_at','TIMESTAMP']]},
  {name:'order_item', group:'commerce', x:1390,y:500, fields:[['PK','id','BIGINT'],['FK','order_id','BIGINT'],['FK','product_id','BIGINT'],['','unit_price','DECIMAL(19,0)'],['','quantity','INTEGER']]},
  {name:'concert', group:'concert', x:710,y:850, fields:[['PK','id','BIGINT'],['FK','channel_id','BIGINT'],['','title','VARCHAR(200)'],['','venue','VARCHAR(300)'],['','concert_at','TIMESTAMP'],['','reservation_start_at','TIMESTAMP'],['','reservation_end_at','TIMESTAMP'],['','status','ON_SALE|STOPPED']]},
  {name:'seat', group:'concert', x:1050,y:850, fields:[['PK','id','BIGINT'],['FK','concert_id','BIGINT'],['','section','VARCHAR(50)'],['','seat_sequence','INTEGER'],['','seat_label','VARCHAR(50)'],['','grade','VARCHAR(50)'],['','price','DECIMAL(19,0)'],['','status','AVAILABLE|RESERVED'],['UQ','concert + section + sequence','COMPOSITE']]},
  {name:'reservation', group:'concert', x:1390,y:850, fields:[['PK','id','BIGINT'],['FK','user_id','BIGINT'],['FK','concert_id','BIGINT'],['FK','seat_id','BIGINT'],['','status','RESERVED|CANCELLED'],['','reserved_at','TIMESTAMP'],['','cancelled_at','TIMESTAMP NULL'],['','reserved_price','DECIMAL(19,0)']]},
];

const esc = s => String(s).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;');
const cardWidth = 285;
const headerHeight = 42;
const rowHeight = 22;
const cardHeight = t => headerHeight + 12 + t.fields.length * rowHeight;
const byName = Object.fromEntries(tables.map(t => [t.name,t]));
const left = name => ({x:byName[name].x, y:byName[name].y + cardHeight(byName[name])/2});
const right = name => ({x:byName[name].x+cardWidth, y:byName[name].y + cardHeight(byName[name])/2});
const top = name => ({x:byName[name].x+cardWidth/2, y:byName[name].y});
const bottom = name => ({x:byName[name].x+cardWidth/2, y:byName[name].y+cardHeight(byName[name])});

let svg = `<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="1750" height="1280" viewBox="0 0 1750 1280">
<defs><filter id="s" x="-20%" y="-20%" width="140%" height="140%"><feDropShadow dx="0" dy="3" stdDeviation="4" flood-color="#0f172a" flood-opacity=".13"/></filter>
<marker id="n" markerWidth="12" markerHeight="12" refX="10" refY="6" orient="auto"><path d="M1 1L10 6L1 11M1 6H10" fill="none" stroke="#64748b" stroke-width="1.4"/></marker></defs>
<rect width="1750" height="1280" fill="#f8fafc"/>
<text x="35" y="45" font-family="Malgun Gothic,Arial" font-size="30" font-weight="700" fill="#0f172a">BERRIES 데이터베이스 ERD</text>
<text x="35" y="72" font-family="Malgun Gothic,Arial" font-size="14" fill="#64748b">팬 커뮤니티 · 굿즈 주문 · 공연 예매 · JWT 인증 | PK/FK 및 핵심 비즈니스 필드</text>
<text x="35" y="108" font-family="Arial" font-size="14" font-weight="700" fill="#7c3aed">AUTH / USER</text>
<text x="375" y="108" font-family="Arial" font-size="14" font-weight="700" fill="#0f766e">CHANNEL</text>
<text x="715" y="108" font-family="Arial" font-size="14" font-weight="700" fill="#2563eb">COMMUNITY</text>
<text x="715" y="480" font-family="Arial" font-size="14" font-weight="700" fill="#c2410c">COMMERCE</text>
<text x="715" y="830" font-family="Arial" font-size="14" font-weight="700" fill="#be123c">CONCERT / RESERVATION</text>`;

function relation(a,b,mode='horizontal',label='1 : N',dash=false) {
  const p1 = mode==='vertical' ? bottom(a) : right(a);
  const p2 = mode==='vertical' ? top(b) : left(b);
  const mid = mode==='vertical' ? (p1.y+p2.y)/2 : (p1.x+p2.x)/2;
  const d = mode==='vertical'
    ? `M${p1.x},${p1.y} V${mid} H${p2.x} V${p2.y}`
    : `M${p1.x},${p1.y} H${mid} V${p2.y} H${p2.x}`;
  svg += `<path d="${d}" fill="none" stroke="#64748b" stroke-width="1.55" ${dash?'stroke-dasharray="5 4"':''} marker-end="url(#n)"/>`;
  const tx = mode==='vertical' ? p1.x+7 : p1.x+7, ty = mode==='vertical' ? mid-5 : p1.y-6;
  svg += `<text x="${tx}" y="${ty}" font-family="Arial" font-size="11" font-weight="700" fill="#64748b">${label}</text>`;
}

function refPill(tableName, label, offset, pillWidth=82) {
  const t=byName[tableName], h=cardHeight(t), cardX=t.x+offset;
  const pillX=cardX-pillWidth/2, pillY=t.y+h+14;
  svg += `<path d="M${cardX},${t.y+h} V${pillY}" fill="none" stroke="#64748b" stroke-width="1.4" marker-end="url(#n)"/>`;
  svg += `<rect x="${pillX}" y="${pillY}" width="${pillWidth}" height="25" rx="12.5" fill="#fff" stroke="#64748b" stroke-width="1.2" stroke-dasharray="4 3"/>`;
  svg += `<text x="${cardX}" y="${pillY+17}" text-anchor="middle" font-family="Consolas,Malgun Gothic" font-size="11" font-weight="700" fill="#475569">REF ${label}</text>`;
}

svg += `<path d="M315,525 H340 V205 H315" fill="none" stroke="#64748b" stroke-width="1.55"/>`;
svg += `<text x="318" y="515" font-family="Arial" font-size="11" font-weight="700" fill="#64748b">1 : 1</text>`;
relation('channel','channel_user','vertical');
svg += `<path d="M655,205 H680 V580 H655" fill="none" stroke="#64748b" stroke-width="1.55" marker-end="url(#n)"/>`;
svg += `<text x="660" y="195" font-family="Arial" font-size="11" font-weight="700" fill="#64748b">1 : N</text>`;
relation('channel','post'); relation('post','comment');
relation('orders','order_item');
relation('concert','seat'); relation('seat','reservation');

// Every non-adjacent FK is connected to a local REF alias of the same parent table.
refPill('channel_user','users',230);
refPill('fan_membership','users',230);
refPill('post','users',225);
// Keep the author reference on the left so it does not collide with the
// self-reference loop drawn on the right side of the comment card.
refPill('comment','users',90);
refPill('post_like','post',85);
refPill('post_like','users',215);
refPill('product','channel',225);
refPill('orders','users',225);
refPill('order_item','product',225);
refPill('concert','channel',225);
refPill('reservation','users',80);
refPill('reservation','concert',215);

// Comment self references (root_comment_id, reply_to_comment_id) use an exterior loop.
const commentTable=byName.comment, commentBottom=commentTable.y+cardHeight(commentTable);
svg += `<path d="M${commentTable.x+250},${commentBottom} V${commentBottom+36} H${commentTable.x+300} V${commentTable.y+175} H${commentTable.x+285}" fill="none" stroke="#64748b" stroke-width="1.4" stroke-dasharray="5 4" marker-end="url(#n)"/>`;
svg += `<text x="${commentTable.x+252}" y="${commentBottom+31}" font-family="Arial" font-size="11" font-weight="700" fill="#64748b">SELF 1 : N</text>`;

for (const t of tables) {
  const [head,border] = colors[t.group]; const h=cardHeight(t);
  svg += `<g filter="url(#s)"><rect x="${t.x}" y="${t.y}" width="${cardWidth}" height="${h}" rx="9" fill="#fff" stroke="${border}" stroke-width="1.5"/>`;
  svg += `<path d="M${t.x+9},${t.y} H${t.x+cardWidth-9} Q${t.x+cardWidth},${t.y} ${t.x+cardWidth},${t.y+9} V${t.y+headerHeight} H${t.x} V${t.y+9} Q${t.x},${t.y} ${t.x+9},${t.y}" fill="${head}"/>`;
  svg += `<text x="${t.x+14}" y="${t.y+27}" font-family="Consolas,Malgun Gothic" font-size="17" font-weight="700" fill="#fff">${esc(t.name)}</text>`;
  t.fields.forEach((f,i)=>{ const y=t.y+headerHeight+22+i*rowHeight; const keyColor=f[0]==='FK'?'#2563eb':'#b45309';
    svg += `<text x="${t.x+12}" y="${y}" font-family="Consolas,Malgun Gothic" font-size="12" font-weight="700" fill="${keyColor}">${esc(f[0])}</text>`;
    svg += `<text x="${t.x+40}" y="${y}" font-family="Consolas,Malgun Gothic" font-size="12.5" fill="#334155">${esc(f[1])}</text>`;
    svg += `<text x="${t.x+cardWidth-10}" y="${y}" text-anchor="end" font-family="Consolas,Malgun Gothic" font-size="10.5" fill="#94a3b8">${esc(f[2])}</text>`;
  }); svg += `</g>`;
}

svg += `<rect x="30" y="1205" width="1645" height="48" rx="8" fill="#fff" stroke="#e2e8f0"/>
<text x="50" y="1235" font-family="Malgun Gothic,Arial" font-size="13" fill="#475569"><tspan font-weight="700" fill="#b45309">PK</tspan> Primary Key　 <tspan font-weight="700" fill="#2563eb">FK</tspan> Foreign Key　 1 : &lt; N 관계　 <tspan font-weight="700">REF</tspan>는 선 교차를 없애기 위한 동일 테이블 참조 별칭　 BaseEntity 감사 필드는 일부 생략</text></svg>`;
fs.writeFileSync(output, svg, 'utf8');
console.log(output);
