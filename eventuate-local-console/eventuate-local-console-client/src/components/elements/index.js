/**
 * Created by andrew on 11/15/16.
 */
import React, { Component, PropTypes } from 'react'; //eslint-disable-line no-unused-vars
import cx from 'classnames';
import * as BS from 'react-bootstrap';
import { LinkContainer } from "react-router-bootstrap";
import TimeAgo from 'react-timeago';
import JSONTree from 'react-json-tree';
import dateFormat from 'dateformat';


const preventDefault = (evt) => {
  evt.preventDefault();
  evt.stopPropagation();
};

export const LoadingIcon = ({ isHidden, isLoading, refresh, ...props }) => {
  return (!isHidden && <BS.Button bsStyle="link" onClick={ refresh } {...props}><BS.Glyphicon glyph="refresh" className={ cx({ 'icon-refresh-animate' : isLoading })} /></BS.Button>);
};

export const CancelButton = ({ isHidden, to }) => {
  return (!isHidden && <LinkContainer to={ to } ><BS.Button bsStyle="link"><BS.Glyphicon glyph="remove" /></BS.Button></LinkContainer>);
};

export const SortToggler = ({ isHidden, sortState, toggleSort, children, inline, className = 'element-sort-toggler' }) => {
  const sortingSymbol = ((sortState) => {
    switch (sortState) {
      case -1: // desc
        return (<BS.Glyphicon glyph="chevron-down" />);
      case 1: // asc
        return (<BS.Glyphicon glyph="chevron-up" />);
      case 0: // none
      default:
        return;
    }
  })(sortState);

  if (inline) {
    return (!isHidden &&  <span className={ className } role="tab" onClick={ toggleSort }>{children} <span className="">{ sortingSymbol }</span></span>);
  }
  return (!isHidden &&  <div className={ className } role="tab" onClick={ toggleSort }>{children} <span className="">{ sortingSymbol }</span></div>);
};

export const getSortTogglerFor = (field, text, props) => {
  const { sorting, sortingKey, sortBy, ...rest } = props;
  const sortTogglerProps = {
    ...rest,
    sortState: (sortingKey === field) ? sorting : null,
    toggleSort: () => sortBy(field)
  };
  return <SortToggler {...sortTogglerProps}>{ text }</SortToggler>
};

export const SearchField = ({ isHidden, state, onChange, reset }) => {
  return (!isHidden && <BS.FormGroup>
    <BS.InputGroup>
      <BS.InputGroup.Addon><BS.Glyphicon glyph="search"/></BS.InputGroup.Addon>
      <BS.FormControl type="text" {...state} onChange={ onChange } />
      <BS.InputGroup.Button><BS.Button onClick={ reset } ><BS.Glyphicon glyph="remove"/></BS.Button></BS.InputGroup.Button>
    </BS.InputGroup>
  </BS.FormGroup>)
};

const formatter = (value, unit, suffix, date) => {
  switch (unit) {
    case 'second':
    {
      if (suffix == 'ago') {
        return 'recently';
      }
    }
    default:
      return TimeAgo.defaultProps.formatter(value, unit, suffix, date);
  }
};

export const Timeago = ({ timestamp, expanded, still, ...props }) => {
  const title = !!timestamp && new Date(timestamp).toLocaleString();
  if (still) {
    return (!!expanded && !!timestamp && <span {...props}>${ dateFormat(new Date(timestamp, "mmmm d, yyyy, HH:MM:ss")) }</span>);;
  }
  if (expanded) {
    return (!!timestamp && <span {...props}><time>{ dateFormat(new Date(timestamp), "mmm d, yyyy, HH:MM:ss") }</time> (<TimeAgo date={timestamp} minPeriod={ 60 } formatter={ formatter } title={ title } className="text-muted" />)</span>);
  }
  return (!!timestamp && <TimeAgo date={timestamp} minPeriod={ 60 } formatter={ formatter } title={ title } className="text-capitalize" />);
};

export const Jsontree = ({ data, hideRoot }) => {
  const theme = {
    tree: {
       backgroundColor: 'transparent',
      margin: '0 .25em'
    },
    arrow: ({ style }, nodeType, expanded) => ({
      style: {
        ...style,
        marginLeft: 0,
        transition: '150ms',
        WebkitTransition: '150ms',
        MozTransition: '150ms',
        WebkitTransform: expanded ? 'rotateZ(90deg)' : 'rotateZ(0deg)',
        MozTransform: expanded ? 'rotateZ(90deg)' : 'rotateZ(0deg)',
        transform: expanded ? 'rotateZ(90deg)' : 'rotateZ(0deg)',
        transformOrigin: '45% 50%',
        WebkitTransformOrigin: '45% 50%',
        MozTransformOrigin: '45% 50%',
        position: 'relative',
        lineHeight: '1.1em',
        fontSize: '0.75em',
        fontFamily: 'sans-serif'
      }
    })
  };
  const shouldExpandNode = (keyName, data, level) => {
    if (level === 0 && keyName == 'root') {
      return false;
    }
    return true;
  };

  let moreProps = {};
  if (hideRoot === false) {
    moreProps = {
      hideRoot,
      shouldExpandNode
    }
  }
  return (!!data && <JSONTree data={ data } theme={ theme } shouldExpandNode={ shouldExpandNode } hideRoot {...moreProps} />);
};

export const errorMessage = (lastError) => (lastError && <BS.Alert bsStyle="danger">
  <h4>Oh snap! You got an error!</h4>
  <p>{ lastError }</p>
</BS.Alert>);

export const updatedWhen = (lastUpdate) => ((lastUpdate !== 0) && <span className="updated-when">Updated: <strong><Timeago timestamp={ lastUpdate } /></strong></span>);

export const foundEntriesHint = (items, text) => (!!(items.length) && <span className="text-muted">({ items.length } { text ? text : 'entries'})</span>);

export const showType = (type) => {
  if (!type) {
    return false;
  }
  const [ visible, ...rest ] = type.split('.').reverse();
  const hidden = rest.reverse().join('.');
  return <span className="element-type"><span className="text-muted">{ hidden }</span>{ visible }</span>
};

export const indicator = (onOff, title) => {
  return (<span>{
    !!title && (title + ' ')
  }<BS.Glyphicon glyph="flash" className={ cx({
    'text-muted': !onOff,
    'text-success': !!onOff,
    'icon-network-animate': !!onOff
  }) } /></span>);
};

export * from './utilities';
export { TypesSelectedDialog } from './TypesSelectedDialog';
export { ScrollablePane } from './ScrollablePane';