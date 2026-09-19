import * as runtime from '../../../build/compileSync/js/main/productionLibrary/kotlin/tauqeet-kmp-shared.js';

const jsApi =
  runtime?.com?.tauqeet?.library?.js ??
  runtime?.default?.com?.tauqeet?.library?.js ??
  {};

export const PrayerTimesJsResult = jsApi.PrayerTimesJsResult ?? null;
export const TauqeetJsBridge = jsApi.TauqeetJsBridge ?? null;
export const calculatePrayerTimesKmp = jsApi.calculatePrayerTimesKmp ?? null;

export default jsApi;
