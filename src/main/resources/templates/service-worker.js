if(!self.define){let e,i={};const n=(n,r)=>(n=new URL(n+".js",r).href,i[n]||new Promise((i=>{if("document"in self){const e=document.createElement("script");e.src=n,e.onload=i,document.head.appendChild(e)}else e=n,importScripts(n),i()})).then((()=>{let e=i[n];if(!e)throw new Error(`Module ${n} didn’t register its module`);return e})));self.define=(r,o)=>{const s=e||("document"in self?document.currentScript.src:"")||location.href;if(i[s])return;let l={};const t=e=>n(e,s),f={module:{uri:s},exports:l,require:t};i[s]=Promise.all(r.map((e=>f[e]||t(e)))).then((e=>(o(...e),l)))}}define(["./workbox-9a84fccb"],(function(e){"use strict";self.addEventListener("message",(e=>{e.data&&"SKIP_WAITING"===e.data.type&&self.skipWaiting()})),e.precacheAndRoute([{url:"060b2710bdbbe3dfe48b.svg",revision:null},{url:"4692b9ec53fd5972caa2.ttf",revision:null},{url:"5be1347c682810f199c7.eot",revision:null},{url:"82b1212e45a2bc35dd73.woff",revision:null},{url:"be810be3a3e14c682a25.woff2",revision:null},{url:"index.html",revision:"b38db379ae5bf4d3b394752a20fe0b4b"},{url:"main.js",revision:"322ba9797a12cfee38095a95f7914dcd"},{url:"main.js.LICENSE.txt",revision:"4e0e34f265fae8f33b01b27ae29d9d6f"}],{})}));
