import express from 'express';
import path from 'path';
import favicon from 'serve-favicon';
import morgan from 'morgan';
import cookieParser from 'cookie-parser';
import bodyParser from 'body-parser';
import { getLogger } from './utils/logger';
import api from './routes/api';

const logger = getLogger();
const publicFolder = path.join(__dirname, '..', 'public');

export const app = express();

// view engine setup
app.set('views', path.join(__dirname, '..', 'src', 'views'));
app.set('view engine', 'jade');

// uncomment after placing your favicon in /public
app.use(favicon(path.join(publicFolder, 'favicon.ico')));
app.use(morgan('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());

// app.configure()
app.use(express.static(publicFolder));

app.use('/api', api);


app.get('*', (req, res, next) => {
  res.sendFile('index.html', { root: publicFolder });
});

// catch 404 and forward to error handler
app.use((req, res, next) => {
  const err = new Error('Resource not found');
  err.status = 404;
  next(err);
});

// error handler
app.use((err, req, res, next) => {

  logger.error(err);

  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});
