import fetchRoute from './fetchRoute'

const reducer = ([prev, promises], next) => {
  if (!prev) return [next, promises];
  const coords = [
    Object.values(prev).reverse(),
    Object.values(next).reverse()
  ];
  const promise = fetchRoute(coords).then((res) => ({ ...res, coords: coords }));
  return [next, [...promises, promise]]
}

const buildPath = (path) => Promise.all(
  path.reduce(
    reducer,
    [undefined, []]
  )[1]
);

export default buildPath;
