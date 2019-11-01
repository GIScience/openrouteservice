import fetchRoute from './fetchRoute'

const reducer = ([prev, promises], next) => {
  if (!prev) return [next, promises];
  const promise = fetchRoute([
    Object.values(prev).reverse(),
    Object.values(next).reverse()
  ]);
  return [next, [...promises, promise]]
}

const buildRoutePath = (path) => Promise.all(
  path.reduce(
    reducer,
    [undefined, []]
  )[1]
);

export default buildRoutePath;
