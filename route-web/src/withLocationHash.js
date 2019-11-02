import React from 'react';

const withLocationHash= (Component) =>
  class extends React.Component {
    constructor(props) {
      super(props);
      const { hash } = global.location;
      this.state = { locationHash: hash.substr(1) }
    }

    render() {
      return <Component
        locationHash={this.state.locationHash}
        {...this.props}
      />
    }
  };

export default withLocationHash;
