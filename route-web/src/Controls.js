import React from 'react';

const ControlsPanel = ({ children }) => (
  <div
    style={{
      position: 'absolute',
      top: '10px',
      right: '10px',
      zIndex: 1000,
      border: '1px solid gray',
      padding: '10px',
      background: '#ddd',
    }}
  >
    <div style={{
      display: 'flex',
      flexDirection: 'column',
    }}>
      {children}
    </div>
  </div>
);

const Button = ({ onClick, text }) => (
  <div style={{flex: '1 1 100%'}}>
    <button
      style={{
        width: '100%',
        padding: '10px 0',
        margin: '0 0 5px',
      }}
      onClick={onClick}
  >
    {text}
  </button>
  </div>
)

export { ControlsPanel, Button };
