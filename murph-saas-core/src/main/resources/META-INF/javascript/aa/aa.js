export default function rest({ rest, logger }) {
    logger.info('请求动态规则: path = {}', rest.path());
    rest.sendJSON({a: Date.now()});
};

export const test = ({ rest }) => {
    rest.sendJSON({endpoint: 'aa/aa.js - test v1'});
}
