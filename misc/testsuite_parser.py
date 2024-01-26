import json
import os
from itertools import islice

test_dir = os.path.join(os.getcwd(), "../examples/testsuite/")
class_dir = os.path.join(os.getcwd(), "../wasp/src/test/java/rrampage/wasp/testsuite/")
import_boilerplate = """package rrampage.wasp.testsuite;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import rrampage.wasp.data.AssertReturn;
import rrampage.wasp.instructions.ConstInstruction;

import java.util.stream.Stream;

import static rrampage.wasp.utils.ConversionUtils.constOf;
"""

testarray_boilerplate = 'private static final AssertReturn[] VAR = new AssertReturn[] { DATA };'


def chunk(it, size):
    it = iter(it)
    return iter(lambda: tuple(islice(it, size)), ())


def get_test_json_path(fname):
    return test_dir + fname


def read_json_file(fpath):
    with open(fpath) as f:
        return json.load(f)


def parse_json(js):
    src_name = js['source_filename'].split('/')[-1].split('.wast')[0].title()
    commands = js['commands']
    module = None
    cmds = {}
    for c in commands:
        if c['type'] == 'module' and module is None:
            module = c['filename']
            continue
        if c['type'] == 'assert_return':
            field = c['action']['field']
            if field not in cmds:
                cmds[field] = []
            cmds[field].append(parse_assert_return(c))
    return src_name, module, cmds


def generate_testcase(field, vals):
    return testarray_boilerplate.replace("VAR", generate_variable(field)).replace("DATA", ",\n\t\t".join(vals))


def generate_variable(field):
    return f'assertReturnTestCases{field.title()}'


def generate_test_method(field):
    return f"""\t@TestFactory public Stream<DynamicTest> test{field.title()}() {{ return runner.test({generate_variable(field)});}}"""


def generate_boilerplate(src_name, module, cmds):
    test_cases = '\n\t'.join([generate_testcase(field, vals) for (field, vals) in cmds.items()])
    test_methods = '\n\t'.join([generate_test_method(field) for field in cmds])
    class_boilerplate = f"""{import_boilerplate}
public class {src_name}Test {{
    {test_cases}
    TestSuiteRunner runner = new TestSuiteRunner("./testsuite/{module}");
    {test_methods}
}}"""
    return class_boilerplate


def parse_assert_return(asr):
    # AssertReturn("fac-iter", ConstInstruction.of(constOf(25L)), ConstInstruction.of(constOf(7034535277573963776L)))
    field = asr['action']['field']
    args = parse_exprs(asr['action']['args'])
    expected = parse_exprs(asr['expected'])
    return f'new AssertReturn("{field}", ConstInstruction.of({", ".join(args)}), ConstInstruction.of({", ".join(expected)}))'


def parse_exprs(exprs):
    return [generate_expr(e['type'], e['value']) for e in exprs]


def generate_expr(t, v):
    #  constOf(Integer.parseUnsignedInt("4294967294"))
    n = v
    if t == 'i32':
        n = int(v)
        if n > 2**31 - 1:
            n = f'Integer.parseUnsignedInt("{v}")'
    if t == 'i64':
        n = int(v)
        n = f'Long.parseUnsignedLong("{n}")' if n > 2**63 - 1 else f'{v}L'
    if t == 'f32':
        n = f'Float.parseFloat("{v}")'
    if t == 'f64':
        n = f'Double.parseDouble("{v}")'
    return f'constOf({n})'


def process(fname):
    js = read_json_file(get_test_json_path(fname))
    src_name, module, cmds = parse_json(js)
    class_file = generate_boilerplate(src_name, module, cmds)
    src_file = f'{class_dir}{src_name}Test.java'
    print(src_file)
    print(class_file)
    """if not os.path.exists(src_file):
        print(f'Saving to {src_file}')
        with open(src_file, "w") as text_file:
            text_file.write(class_file)
    else:
        print("File already exists")"""


def main():
    process('loop.json')


if __name__ == '__main__':
    main()
