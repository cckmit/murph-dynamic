export default function rest({ rest }) {
    rest.sendJSON({});
};

export const test = ({ rest }) => {
    rest.sendJSON({endpoint: 'aa/aa.js - test v1'});
}
