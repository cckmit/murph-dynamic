export default (rest) => {
    logger.info('hello, {}', Date.now());
    return rest.json({ a: 1 });
}
