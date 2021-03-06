import java.io.FileNotFoundException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

class Compiler {
    private String _moduleName;
    private Parser _parser;
    private int _nextLabel = 0;
    private List<Integer> _loopEndLabels = new ArrayList<>();

    private Module _module = new Module();
    private List<Module> _imports = new ArrayList<>();
    private HashMap<String, Compiler> _units;

    public Compiler(String moduleName, HashMap<String, Compiler> units)
        throws FileNotFoundException {

        _moduleName = moduleName;
        _parser = new Parser(moduleName + ".wok");
        _units = units;

        _units.put(_moduleName, this);
        pass1();
    }

    private void pass1()
        throws FileNotFoundException {

        boolean isPrivate = false;
        for (var next = _parser.nextDeclaration();
             next.isPresent();
             next = _parser.nextDeclaration()) {
            var tl = next.get();
            switch (tl.kind()) {
            case WORD:
            case TYPE:
                _module.add(tl, isPrivate);
                break;
            case IMPORT:
                var use = (UseDeclaration) tl;
                if (_units.containsKey(use.name())) {
                    break;
                }
                var compiler = new Compiler(use.name(), _units);
                break;
            case VPUBLIC:
                isPrivate = false;
                break;
            case VPRIVATE:
                isPrivate = true;
                break;
            }
        }
    }

    public void codegen() {
    }
}
