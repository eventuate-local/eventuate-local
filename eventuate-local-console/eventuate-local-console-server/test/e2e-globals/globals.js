/**
 * Created by andrew on 2/2/17.
 */
let nextFilenameIdx = 0;

export default {
  waitForConditionTimeout: 10000,
  // userData,
  // otherUserData,
  // accountOne,
  // accountTwo,
  // transferOne,
  // transferTwo,
  seed: (new Date() - 0).toString(),
  nextFilenameIdx: () => nextFilenameIdx++
};