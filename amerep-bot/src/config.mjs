'use strict';

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const dir = path.resolve(__dirname, '../config');

let config = {};

for (const filename of fs.readdirSync(dir)) {
  if (path.extname(filename) != '.json') {
    continue;
  }
  const filepath = dir + '/' + filename;
  if (fs.lstatSync(filepath).isDirectory()) {
    continue;
  }
  let json = {};
  let key = path.basename(filename, '.json');
  try {
    json = JSON.parse(fs.readFileSync(filepath));
  } catch (error) {
    throw error;
  }
  if (key == 'config') {
    for (const k in json) {
      config[k] = json[k];
    }
  } else {
    config[key] = json;
  }
}

export default config;
