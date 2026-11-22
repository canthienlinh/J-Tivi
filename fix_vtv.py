import json, io

P = r'F:\PROJECT\J-TV\playlist\channels.json'
BASE = "https://toiyeuvietnam.dpdns.org/TuyetDoiKhongKinhDoanh/{}-hd/KenhCoBan.m3u8"

d = json.load(open(P, encoding='utf-8'))

# map ten kenh VTV1-8 -> slug toiyeuvietnam
vtv_slug = {
    "VTV1": "vtv1", "VTV2": "vtv2", "VTV3": "vtv3", "VTV4": "vtv4",
    "VTV5": "vtv5", "VTV7": "vtv7", "VTV8": "vtv8",
}

changed = []
for c in d:
    n = c.get("name", "")
    if n in vtv_slug:
        c["url"] = BASE.format(vtv_slug[n])
        c["referer"] = ""          # nguon nay khong can referer
        c["userAgent"] = ""
        changed.append(n)

# them QPVN neu chua co
names = {c.get("name") for c in d}
if "QPVN" not in names:
    # chen sau VTV9
    idx = next((i for i, c in enumerate(d) if c.get("name") == "VTV9"), len(d))
    qpvn = {
        "name": "QPVN",
        "group": "VTV",
        "logo": "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4d/Logo_QPVN.png/320px-Logo_QPVN.png",
        "tvgId": "QPVN",
        "url": BASE.format("qpvn"),
        "userAgent": "",
        "referer": "",
    }
    d.insert(idx + 1, qpvn)
    changed.append("QPVN(added)")

json.dump(d, open(P, 'w', encoding='utf-8'), ensure_ascii=False, indent=2)

out = io.open(r'F:\PROJECT\J-TV\fix_out.txt', 'w', encoding='utf-8')
out.write("changed: " + ", ".join(changed) + "\n")
out.write("total: %d\n" % len(d))
out.close()
