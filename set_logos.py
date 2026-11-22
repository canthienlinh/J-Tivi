import json, io
P = r'F:\PROJECT\J-TV\playlist\channels.json'
d = json.load(open(P, encoding='utf-8'))
LOGO = {
    "VTV6": "https://upload.wikimedia.org/wikipedia/commons/4/4e/VTV6_new_logo_2026.webp",
    "QPVN": "https://i.imgur.com/GQO7UNu.png",
}
done = []
for c in d:
    n = c.get("name")
    if n in LOGO:
        c["logo"] = LOGO[n]
        done.append(n)
json.dump(d, open(P, 'w', encoding='utf-8'), ensure_ascii=False, indent=2)
io.open(r'F:\PROJECT\J-TV\logo_set.txt','w',encoding='utf-8').write("set: "+", ".join(done)+"\n")
