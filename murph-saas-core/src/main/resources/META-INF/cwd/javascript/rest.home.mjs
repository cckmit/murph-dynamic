export default function rest() {
    return {
        "/hello": (params) => {
            console.log(params)
        },
        "/world": (params) => {
            console.log(params)
        }
    };
};
