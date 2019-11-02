import React from 'react';
import buildPath from './route/buildPath.js';
import get from 'lodash/get';
import slice from 'lodash/slice';

const sameLatlng = (
  { lat: lat1, lng: lng1 },
  { lat: lat2, lng: lng2 }
) => lat1 === lat2 && lng1 === lng2;

const withRoute = (Component) =>
  class extends React.Component {
    constructor(props) {
      super(props);
      this.state = { path: [], lines: [] };
      this.appendPoint = this.appendPoint.bind(this);
      this.movePoint = this.movePoint.bind(this);
      this.undo = this.undo.bind(this);
    }

    updatePath(newPath, setPrevPath) {
      const { path } = this.state;
      const promise = buildPath(newPath);
      promise.then((paths) => {
        const lines = paths.map((p) =>
          get(p, 'features[0].geometry.coordinates')
            .map(([lat, lng]) => [lng, lat]));
        this.setState({ lines });
      });
      if (setPrevPath !== false) newPath.prevPath = path;
      this.setState({ path: newPath });
    }

    undo() {
      const { path } = this.state;
      const { prevPath } = path;
      if (prevPath) {
        prevPath.nextPath = path;
        this.updatePath(prevPath, false);
      }
    }

    movePoint(oldLatlng, newLatlng) {
      const { path } = this.state;
      const index = path.indexOf(oldLatlng);
      const newPath = [
        ...slice(path, 0, index),
        newLatlng,
        ...slice(path, index + 1),
      ];
      this.updatePath(newPath);
    }

    appendPoint({ latlng, after }) {
      const { path } = this.state;
      if (after) {
        console.log(after);
      console.log(path.reduce((memo, ll) => {
          return sameLatlng(ll, after) ?
            [...memo, ll, latlng] : [...memo, ll];
        }, [])) }
      this.updatePath(
        after ?
        path.reduce((memo, ll) => {
          return sameLatlng(ll, after) ?
            [...memo, ll, latlng] : [...memo, ll];
        }, []) : [...path, latlng]
      )
    }

    render() {
      return <Component
        path={this.state.path}
        lines={this.state.lines}
        appendPoint={this.appendPoint}
        movePoint={this.movePoint}
        undo={this.undo}
        {...this.props}
      />
    }
  };

export default withRoute;
