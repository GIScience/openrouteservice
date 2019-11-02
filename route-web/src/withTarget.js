import React from 'react';
import get from 'lodash/get';

const INITIAL_STATE = {
  targetType: undefined,
  targetData: {},
  target: undefined,
};

const withTarget = (Component) =>
  class extends React.Component {
    constructor(props) {
      super(props);
      this.state = INITIAL_STATE;
      this.setTarget = this.setTarget.bind(this);
      this.clearTarget = this.clearTarget.bind(this);
    }

    setTarget(targetType, target, targetData) {
      this.setState({ targetType, targetData, target });
    }

    clearTarget() {
      this.setState(INITIAL_STATE);
    }

    render() {
      return <Component
        target={this.state.target}
        targetType={this.state.targetType}
        targetData={this.state.targetData}
        setTarget={this.setTarget}
        clearTarget={this.clearTarget}
        {...this.props}
      />
    }
  };

export default withTarget;
