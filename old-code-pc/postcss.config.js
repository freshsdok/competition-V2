import postcssPxtorem from 'postcss-pxtorem';

export default {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
    'postcss-pxtorem': {
      rootValue: 16,
      propList: ['*'],
      exclude: /node_modules/i
    }
  }
}
