# passa-paraula

Simple game to play with Clojurescript and Reagent libraries.

## Usage

### Development mode
```
npm install
npx shadow-cljs watch app

Check the game in http://localhost:3000
```
start a ClojureScript REPL
```
npx shadow-cljs browser-repl
```
### Building for production

```
npx shadow-cljs release app
```

## Next steps
- cannot edit directly letters field in preferences page. Workaround, edit the comma-separated letters list on another field, and copy paste it into the letters field.
- test it with: cljs.test
