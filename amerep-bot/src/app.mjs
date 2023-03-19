import config from './config.mjs';

import Date from 'datwo';
import FormData from 'form-data';
import YAML from 'yaml';
import JSZip from 'jszip';

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

let filerx = config.target.name;
filerx = filerx.replace(/\^/g, '\\^');
filerx = filerx.replace(/\$/g, '\\$');
filerx = filerx.replace(/\./g, '\\.');
filerx = filerx.replace(/\+/g, '\\+');
filerx = filerx.replace(/\*/g, '\\*');
filerx = filerx.replace(/\(/g, '\\(');
filerx = filerx.replace(/\)/g, '\\)');
filerx = filerx.replace(/\{/g, '\\{');
filerx = filerx.replace(/\}/g, '\\}');
filerx = filerx.replace(/\[/g, '\\[');
filerx = filerx.replace(/\]/g, '\\]');
filerx = filerx.replace('\\{version\\}', '(.*)');
filerx = `^${filerx}$`;
const fileRegExp = new RegExp(filerx);

function find() {
  for (const filename of fs.readdirSync(config.target.dir)) {
    const matches = filename.match(fileRegExp);
    if (matches) {
      const filepath = path
        .resolve(config.target.dir, filename)
        .replace(/\\/g, '/');
      if (fs.statSync(filepath).size / 1024 > config.target.minkb) {
        return {
          path: filepath,
          file: filename,
          version: matches[1],
        };
      }
    }
  }
  return null;
}

function parse(target) {
  let channel = null;
  for (const chan in config.channels) {
    const condition = config.channels[chan].condition;
    if (condition && new RegExp(condition).test(target.version)) {
      channel = chan;
    }
  }

  channel = channel ? channel : 'default';

  return {
    channel: config.channels[channel].name,
    version: formatVersion(config.channels[channel].version, target),
  };

  function formatVersion(format, target) {
    format = format.replace(/\{version\}/g, target.version);
    for (var tm of format.match(/\{timestamp:(.+)\}/g) || []) {
      var m = tm.match(/\{timestamp:(.+)\}/);
      format = format.replace(m[0], new Date().format(m[1]));
    }
    return format;
  }
}

async function post(data) {
  const p = fs.createReadStream(data.path);

  const form = new FormData();

  form.append('version', data.version);
  form.append('package', p);

  return new Promise((resolve, reject) => {
    form.submit(
      {
        protocol: 'https:',
        host: config.repo.host,
        path: config.repo.path + '/' + config.repo.name + '/' + data.channel,
        headers: {
          o: config.repo.key,
        },
      },
      (error, res) => {
        error
          ? reject(error)
          : res.statusCode >= 400
          ? reject(`HTTP ${res.statusCode}`)
          : resolve();
      }
    );
  });
}

const repackages = {};
repackages.bukkitplugin = async (target, data) => {
  const pkg = new JSZip();
  await pkg.loadAsync(fs.readFileSync(target.path));

  const d = path.resolve(__dirname, '../data');
  fs.mkdirSync(d, { recursive: true });
  const p = path.resolve(d, Date.now() + '');

  let plugin_yml = YAML.parse(await pkg.file('plugin.yml').async('string'));
  plugin_yml.version = data.version;
  await pkg.file('plugin.yml', YAML.stringify(plugin_yml));

  return await new Promise((resolve) => {
    pkg
      .generateNodeStream({
        type: 'nodebuffer',
        compression: 'DEFLATE',
        compressionOptions: {
          level: 9,
        },
        streamFiles: true,
      })
      .pipe(fs.createWriteStream(p))
      .on('finish', () => {
        resolve(p);
      });
  });
};

repackages.paperplugin = async (target, data) => {
  const pkg = new JSZip();
  await pkg.loadAsync(fs.readFileSync(target.path));

  const d = path.resolve(__dirname, '../data');
  fs.mkdirSync(d, { recursive: true });
  const p = path.resolve(d, Date.now() + '');

  let plugin_yml = YAML.parse(await pkg.file('plugin.yml').async('string'));
  plugin_yml.version = data.version;
  await pkg.file('plugin.yml', YAML.stringify(plugin_yml));

  let paper_plugin_yml = YAML.parse(
    await pkg.file('paper-plugin.yml').async('string')
  );
  paper_plugin_yml.version = data.version;
  await pkg.file('paper-plugin.yml', YAML.stringify(paper_plugin_yml));

  return await new Promise((resolve) => {
    pkg
      .generateNodeStream({ streamFiles: true })
      .pipe(fs.createWriteStream(p))
      .on('finish', () => {
        resolve(p);
      });
  });
};

async function run(target) {
  console.log('패키지 파일을 찾았습니다: ' + target.file);

  console.log(
    '패키지 파일 크가: ' + fs.statSync(target.path).size / 1024 + ' KB'
  );

  const data = parse(target);

  console.log('파싱된 버전:');
  console.log('  채널: ' + data.channel);
  console.log('  버전: ' + data.version);

  let repackage = null;

  if (repackages[config.type]) {
    repackage = await repackages[config.type](target, data);
    console.log('리패키지 파일이 생성되었습니다.');
    console.log(
      '리패키지 파일 크가: ' + fs.statSync(repackage).size / 1024 + ' KB'
    );
  }

  await post({
    path: repackage ? repackage : target.path,
    version: data.version,
    channel: data.channel,
  }).catch((error) => {
    if (repackage) {
      fs.unlinkSync(repackage);
      console.log('리패키지 파일이 제거되었습니다.');
    }
    throw error;
  });

  console.log('패키지 업로드가 완료되었습니다.');

  fs.unlinkSync(target.path);

  console.log('패키지 파일이 제거되었습니다.');

  if (repackage) {
    fs.unlinkSync(repackage);
    console.log('리패키지 파일이 제거되었습니다.');
  }
}

let running = false;
setInterval(() => {
  if (running) {
    return;
  }
  running = true;
  let target;
  try {
    target = find();
  } catch (error) {}
  if (target) {
    setTimeout(() => {
      run(target)
        .then(() => {
          console.log('작업이 완료되었습니다.');
          running = false;
        })
        .catch((error) => {
          console.error('오류:', error);
          running = false;
        });
    }, config.target.delay);
  } else {
    running = false;
  }
}, config.target.delay);
