import java.util.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;


public class Linker_yy2285 {
    
    //private static String symbolTable[];//Store all the symbols
    private static LinkedHashMap<String,Integer> symbolTable = new LinkedHashMap<>();
    //private static int symbolValue[];//Store the value of each symbol
    private static HashMap<String,Integer> definedModule = new HashMap<>();//store the module where symbol first defined
    private static HashMap<String,Integer> notActualUsedList = new HashMap<>();//store the unused symbol in the module
    private static HashMap<String,Integer> exceedModule = new HashMap<>();//store the symbols definition exceeds the module size
    private static ArrayList<String> multiDefined = new ArrayList<>();
    private static ArrayList<String> used = new ArrayList<>();//store all the symbols have been used ( appear in use list)
    private static ArrayList<String> notDefined = new ArrayList<>();//store symbols used but not defined
    private static int start = 0;
    private static int index = 0;
    private static int machineSize = 0;
    
    //Test if the symbol is in the symbol table
    
    private static int defined(String s, Integer i) {
        if(symbolTable.containsKey(s)) {
            return symbolTable.get(s);
        }
        return -1;
    }
    
    private static void symbolNotUsed(ArrayList<String> used, LinkedHashMap<String, Integer> symbolTable, HashMap<String, Integer> definedModule) {
        Iterator it = symbolTable.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if(!used.contains(pair.getKey()))
                System.out.println("Warning: " + pair.getKey() + " was defined in module " + definedModule.get(pair.getKey()) +" but never used.");
            //System.out.println(pair.getKey()+" = "+pair.getValue());
            
        }
    }
    
    //let notActualUsedList only store symbols not used in this module
    private static void listNotUsed(ArrayList<String> notActualUsed, Integer module) {
        for(int i=0; i<notActualUsed.size(); i++) {
            notActualUsedList.put(notActualUsed.get(i), module);
        }
    }
    
    //print out symbols not used but appear in the use list
    private static void listNotUsedWarning(HashMap<String,Integer> notActualUsedList) {
        Iterator it = notActualUsedList.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println("Warning: In module " + pair.getValue() +" "+ pair.getKey() + " appeared in the use list but was not actually used.");;
        }
    }
    
    private static void notDefinedError(ArrayList<String> notDefined, String symbol) {
        if(notDefined.contains(symbol)) {
            System.out.printf("  Error:  " + symbol + " is used but not defined; zero used.\n");
        }
        else
            System.out.printf("\n");
    }
    
    private static void exceedModuleError(HashMap<String,Integer> exceedModule) {
        Iterator it = exceedModule.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println("Error: In module " + pair.getValue() + " the def of " + pair.getKey() + " exceeds the module size; zero (relative) used.");
        }
    }
    
    public static void main(String[] args) throws FileNotFoundException {
        
        System.out.println("Please enter the file name with extension: ");
        Scanner input = new Scanner(System.in);
        File file = new File(input.nextLine());
        //File file = new File("/Users/yyt/Desktop/input8.txt");
        //first pass
        System.out.printf("Symbol Table");
        Scanner scanner1 = new Scanner(file);
        
        int module = scanner1.nextInt();//total number of modules
        int moduleBase[] = new int[module];
        int moduleEnd[] = new int[module];
        //System.out.println("read number of modules: "+module);
        
        for(int i=0; i<module; i++) {
            //first line    : definition of symbols
            moduleBase[i] = start;//Base address for this module
            int count1 = scanner1.nextInt();//number of symbols defined
            if(count1!=0) {
                for(int j=0; j<count1; j++) {
                    String symbol = scanner1.next();
                    if(defined(symbol,i)==-1) {
                        symbolTable.put(symbol, scanner1.nextInt()+start);
                        definedModule.put(symbol, i);
                    }
                    else {
                        multiDefined.add(symbol);
                        //System.out.println("The symbol " + symbol +" is multiply defined! Use the first definition.");
                    }
                }
            }
            //skip second line: symbol uses in this module
            int count2 = scanner1.nextInt();
            for(int a=0; a<count2; a++) {
                String temp1 = scanner1.next();
            }
            //third line: program text
            int count3 = scanner1.nextInt();
            start += count3;
            moduleEnd[i] = start;
            Iterator it = definedModule.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if(pair.getValue().equals(i)) {
                    if(symbolTable.get(pair.getKey())>moduleEnd[i]) {
                        symbolTable.put(String.valueOf(pair.getKey()), moduleBase[i]);
                        exceedModule.put(String.valueOf(pair.getKey()), i);
                    }
                }
            }
            for(int b=0; b<count3; b++) {
                String temp2 = scanner1.next();
                int temp3 = scanner1.nextInt();
            }
            //System.out.println("Module " + i +" started at " + moduleBase[i]);
        }
        machineSize = start;
        Iterator it = symbolTable.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String newLine = System.getProperty("line.separator");
            System.out.printf(newLine + pair.getKey()+"="+pair.getValue());
            if(multiDefined.contains(pair.getKey()))
                System.out.print(" Error: This variable is multiply defined; first value used.");
            //it.remove();
        }
        
        scanner1.close();
        
        //second pass
        System.out.println("\n\nMemory Map");
        Scanner scanner2 = new Scanner(file);
        module = scanner2.nextInt();
        for(int i=0; i<module; i++) {
            //skip first line    : definition of symbols
            int count2 = scanner2.nextInt();
            for(int a=0; a<count2; a++) {
                String temp = scanner2.next();
                int tempValue = scanner2.nextInt();
                /**
                 if(tempValue>moduleEnd[i]-moduleBase[i]) {
                 
                 symbolTable.put(temp, moduleBase[i]);
                 exceedModule.put(temp, i);
                 //System.out.println("Error: "+temp+"'s address in the difinition exceeds the size of the module, treated as 0 ");
                 }
                 **/
            }
            //second line: symbol uses in this module
            int num = scanner2.nextInt(); //number of symbol used in this module
            String symbolUsed[] = new String[num];
            ArrayList<String> notActualUsed = new ArrayList<>();
            for(int j=0; j<num; j++) {
                symbolUsed[j] = scanner2.next();
                notActualUsed.add(symbolUsed[j]);
                if(!symbolTable.containsKey(symbolUsed[j])) {
                    symbolTable.put(symbolUsed[j], 0);
                    notDefined.add(symbolUsed[j]);
                    //System.out.println("Error:  " + symbolUsed[j] + " is not defined; zero used.");
                }
                used.add(symbolUsed[j]);
            }
            //third line: program text
            int textNum = scanner2.nextInt();
            
            for(int k=0; k<textNum; k++) {
                String addrType = scanner2.next();
                int address = scanner2.nextInt();
                
                switch(addrType) {
                    case "I":
                        System.out.format("%-3d : %5d\n" ,index, address);
                        index++;
                        break;
                    case "A":
                        if(address%1000>=machineSize) {
                            System.out.format("%-3d : %5d " ,index, address/1000*1000);
                            System.out.printf("Error: Absolute address exceeds machine size; zero used.\n");
                        }
                        else
                            System.out.format("%-3d : %5d \n" ,index, address);
                        index++;
                        break;
                    case "R":
                        address += moduleBase[i];
                        if(address%1000>=moduleEnd[i]) {
                            System.out.format("%-3d : %5d Error: Relative address exceeds module size; zero used.\n" ,index, address/1000*1000);
                        }
                        else
                            System.out.format("%-3d : %5d\n" ,index, address);
                        index++;
                        break;
                    case "E":
                        int symbolIndex = address%10;
                        //Error
                        if(symbolIndex>=symbolUsed.length) {
                            System.out.format("%-3d : %5d Error: External address exceeds length of use list; treated as immediate.\n" ,index, address);
                        }
                        else {
                            address = address + symbolTable.get(symbolUsed[symbolIndex]) - symbolIndex;
                            notActualUsed.remove(symbolUsed[symbolIndex]);
                            
                            System.out.format("%-3d : %5d" ,index, address);
                            //Error
                            notDefinedError(notDefined,symbolUsed[symbolIndex]);
                            
                        }
                        index++;
                        break;
                }
            }
            listNotUsed(notActualUsed, i);
        }
        //System.out.println(scanner2.next());
        scanner2.close();
        System.out.println("\n");
        //print out Warning
        symbolNotUsed(used, symbolTable, definedModule);
        listNotUsedWarning(notActualUsedList);
        //print out Error
        exceedModuleError(exceedModule);
        
    }
    
}
