export default ({ rest, logger }) => {
    logger.info('hello, {}', Date.now());
    rest.sendJSON({ request: 'default request' });
}

export const test = ({ rest, logger }) => {
    rest.sendJSON({ request: 'test request' });
}

export const call = ({ rest, logger }) => {
    rest.sendJSON({ request: 'call v1 request' });
}
