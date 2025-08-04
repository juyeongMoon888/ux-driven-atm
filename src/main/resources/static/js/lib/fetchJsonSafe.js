export async function fetchJsonSafe(res) {

    if (!res){
        console.error("fetchJsonSafe에 null 응답이 전달되었습니다.");
        return null;
    }

    try {
        const text = await res.text();
        let data = {};

        try {
            data = JSON.parse(text);
        } catch (e) {
            console.error("파싱 실패", e);
            return { ok: res.ok, raw: text };
        }
        return { ok: res.ok, ...data };
    } catch (e) {
        console.error("네트워크 오류", e);
        throw e;
    }
}