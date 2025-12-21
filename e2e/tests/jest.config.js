/** @type {import('ts-jest').JestConfigWithTsJest} */
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  testMatch: ['**/src/**/*.test.ts'],
  testTimeout: 120000, // 2 minutes - servers can be slow to respond
  verbose: true,
  forceExit: true,
  detectOpenHandles: true,
};
