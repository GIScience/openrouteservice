import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './App';
import * as serviceWorker from './serviceWorker';

const accessToken = process.env.REACT_APP_TILE_SERVICE_KEY;

const theApp = (
  <App
    accessToken={accessToken}
  />
);

ReactDOM.render(
  theApp, document.getElementById('root'));

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
