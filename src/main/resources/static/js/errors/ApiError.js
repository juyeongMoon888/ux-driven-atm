export class ApiError extends Error {
    constructor (code, message, data) {
        super(message);
        this.code = code;
        this.data = data;
        this.name = "ApiError";
    }
}