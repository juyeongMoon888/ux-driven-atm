export async function fetchJsonSafe(url, options = {}) {
    try {
        const res = await fetch(url, options);
        const text = res.text();
        let data = {};

        try {
            data = JSON.parse(text);
        } catch (e) {
            console.error("파싱 실패", e)
            return { ok: res.ok, raw: text };
        }
        return { ok: res.ok, ...data };
    } catch (e) {
        console.error("네트워크 오류", e);
        throw e;
    }
}