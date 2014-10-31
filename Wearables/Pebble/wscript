
#
# This file is the default set of rules to compile a Pebble project.
#
# Feel free to customize this to your needs.
#

try:
    from sh import CommandNotFound, jshint, cat, ErrorReturnCode_2
    hint = jshint
except (ImportError, CommandNotFound):
    hint = None

top = '.'
out = 'build'

def options(ctx):
    ctx.load('pebble_sdk')

def configure(ctx):
    ctx.load('pebble_sdk')
    global hint
    if hint is not None:
        hint = hint.bake(['--config', 'pebble-jshintrc'])

def build(ctx):
    if False and hint is not None:
        try:
            hint([node.abspath() for node in ctx.path.ant_glob("src/**/*.js")], _tty_out=False) # no tty because there are none in the cloudpebble sandbox.
        except ErrorReturnCode_2 as e:
            ctx.fatal("\nJavaScript linting failed (you can disable this in Project Settings):\n" + e.stdout)

    # Concatenate all our JS files (but not recursively), and only if any JS exists in the first place.
    ctx.path.make_node('src/js/').mkdir()
    js_paths = [node.abspath() for node in ctx.path.ant_glob("src/*.js")]
    if js_paths:
        ctx.exec_command(['cat'] + js_paths, stdout=open('src/js/pebble-js-app.js', 'a'))

    ctx.load('pebble_sdk')

    ctx.pbl_program(source=ctx.path.ant_glob('src/**/*.c'),
                    target='pebble-app.elf')

    ctx.pbl_bundle(elf='pebble-app.elf',
                   js=ctx.path.ant_glob('src/js/**/*.js'))

