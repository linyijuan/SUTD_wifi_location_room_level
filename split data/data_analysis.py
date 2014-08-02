import math
#read file
def read_file(filename):
    name = filename+'.txt'
    file = open(name)
    contents = map(str.strip,file.readlines())
    file.close()
    return contents

#Delete the blank line and generate a list with chosen columns
def get_column_values(col_num,lines):
    column_values = []

    for line in lines:
        if not line:
            continue

        values = line.split(',')

        #col_num - 1 = index of list
        column_values.append(values[col_num-1].strip())

    return column_values
    
#Generate a dictionary with key as ssid and value as signal strength
def sort(lines1,lines2):
    new = list(zip(lines1,lines2))

    r = {}
    d = dict()

    
    for item in new:        
        (x,y) = item
        r.setdefault(x,[]).append(y)
        


    for (k,v) in r.items():
        add = 0
        for i in v:
            i = int(i)
            add = add+i
            
        avg = '%.2f' % (add/len(v))
        d[k] = avg
        
        
    return d

#Sort the dictionary from high to low
def high_to_low(content):
    d = content
    sorted_lines = sorted(d.items(), key=lambda d:d[1], reverse=True)
    
    return sorted_lines


#Write into file and edit the format
def write_file(filename,lines,num_lines):
    file = open(filename,"w")
    count = 0
    
    for line in lines:
        file.write("%s  %s\n" % (str(line[0]), str(line[1])))
        count = count+1

        #Take a certain number of lines
        if count>=num_lines:
            break
    file.close()

def getAllSsid(list_of_filenames):
    ssid_record=[]
    for filenames in list_of_filenames:
        for filename in filenames:
            lines = read_file(filename)
            ssid_cv = get_column_values(3,lines)
            for ssid in ssid_cv:
                if ssid not in ssid_record:
                     ssid_record.append(ssid)
    return ssid_record
    
#generate a list of fingerprint in form in dictionary<ssid:signal strength>
def generate_room_fp(filenames,ssid_record):
    
    #Give a number of lines to write into a file
    Lines_into_file = 20
    room_fp=[]
    for filename in filenames:
        
        lines = read_file(filename)
        ssid_cv = get_column_values(3,lines)
                
        lines1 = read_file(filename)
        ss_cv = get_column_values(4,lines1)

        avg_ss = sort(ssid_cv,ss_cv)

        for ssid in ssid_record:
            if not ssid in avg_ss:
                avg_ss[ssid]=0
        room_fp.append(avg_ss)
                        
        inputs = high_to_low(avg_ss)
        
        new_name = 'fp_'+filename+'.txt'
#        write_file(new_name,inputs,Lines_into_file)
    
    return room_fp

#return a list of lists of dicts<ssid:signal strength>
def get_all_room_fp(list_of_filenames):
    ssid_record = getAllSsid(list_of_filenames)
    all_room_fp=[]
    for filenames in list_of_filenames:
        single_room_fp = generate_room_fp(filenames,ssid_record)
        all_room_fp.append(single_room_fp)
    
    print all_room_fp       
    return all_room_fp       


def getCurrentLocation(filenames,ssid_record):
    test_location = {}

    for filename in filenames:
        
        lines = read_file(filename)
        ssid_cv = get_column_values(3,lines)
                
        lines1 = read_file(filename)
        ss_cv = get_column_values(4,lines1)

        avg_ss = sort(ssid_cv,ss_cv)

        for ssid in ssid_record:
            if not ssid in avg_ss:
              avg_ss[ssid]=0

        test_location[filename] = matchLocationByCorrelation(avg_ss)

        print filename+" : "+matchLocationByCorrelation(avg_ss)
        
def matchLocationByDistance(dic):
    room_fp_standard = get_all_room_fp(training_dataset)
    D=["2-1","2-2","space bar","canteen","LT5","Lt4"]
    room_errs=[]
    
    for room in room_fp_standard:
       room_err = []
       for hashmaps in room:
           point_err = 0
           for ssid in hashmaps:
               point_err = point_err + (float(dic[ssid])-float(hashmaps[ssid]))**2
           point_err = math.sqrt(point_err)
           room_err.append(point_err)
       #use the least error in the list->accurancy (87%,93.5%)..depend on data
#       room_errs.append(min(room_err))
       #calculate average errors->accurancy (80%,90.3%)
       room_errs.append(float(sum(room_err))/len(room_err))


        

    for i in range(len(room_errs)):
       if i == 0:
           minimum = i
       else:
           if room_errs[i] < room_errs[minimum]:
               minimum = i
    
    return D[minimum] 

def matchLocationByCorrelation(dic):
    room_fp_standard = get_all_room_fp(training_dataset)
    D=["2-1","2-2","space bar","canteen","LT5","Lt4"]
    room_cors=[]

    for room in room_fp_standard:
       room_cor=[]
       for hashmaps in room:
           point_cor = 0
           for ssid in hashmaps:
              point_cor = point_cor + float(dic[ssid])*float(hashmaps[ssid])
           room_cor.append(point_cor)
       #accurancy (83.8%,70.9%)
       room_cors.append(float(sum(room_cor))/len(room_cor))
       #accurancy (77.4%,90.3%)
#       room_cors.append(max(room_cor))

    
    for i in range(len(room_cors)):
       if i == 0:
           maximum = i
       else:
           if room_cors[i] > room_cors[maximum]:
               maximum = i
    
    return D[maximum]       
       
      

    
    
    

##write the filename, format is like this
training_dataset = [["train_2-1-01","train_2-1-02","train_2-1-03","train_2-1-04","train_2-1-05"],
                ["train_2-2-01","train_2-2-02","train_2-2-03","train_2-2-04","train_2-2-05"],
                ["train_spaceBar-01","train_spaceBar-02","train_spaceBar-03","train_spaceBar-04","train_spaceBar-05"],
                ["train_canteen-01","train_canteen-02","train_canteen-03","train_canteen-04","train_canteen-05","train_canteen-06"],
                ["train_LT-5-01","train_LT-5-02","train_LT-5-03","train_LT-5-04","train_LT-5-05"],
                ["train_LT-4-01","train_LT-4-02","train_LT-4-03","train_LT-4-04","train_LT-4-05"]];

testing_dataset = ['test_2-1-01', 'test_2-1-02', 'test_2-1-03', 'test_2-1-04', 'test_2-1-05', 'test_2-2-01',
                   'test_2-2-02', 'test_2-2-03', 'test_2-2-04', 'test_2-2-05', 'test_spaceBar-01',
                   'test_spaceBar-02', 'test_spaceBar-03', 'test_spaceBar-04', 'test_spaceBar-05',
                   'test_canteen-01','test_canteen-02', 'test_canteen-03', 'test_canteen-04', 'test_canteen-05',
                   'test_canteen-06', 'test_LT-5-01', 'test_LT-5-02', 'test_LT-5-03', 'test_LT-5-04',
                   'test_LT-5-05', 'test_LT-4-01', 'test_LT-4-02', 'test_LT-4-03', 'test_LT-4-04',
                   'test_LT-4-05']

training_dataset = [['test_2-1-01', 'test_2-1-02', 'test_2-1-03', 'test_2-1-04', 'test_2-1-05'],
                    ['test_2-2-01', 'test_2-2-02', 'test_2-2-03', 'test_2-2-04', 'test_2-2-05'],
                    ['test_spaceBar-01', 'test_spaceBar-02', 'test_spaceBar-03', 'test_spaceBar-04', 'test_spaceBar-05'],
                    ['test_canteen-01', 'test_canteen-02', 'test_canteen-03', 'test_canteen-04', 'test_canteen-05', 'test_canteen-06'],
                    ['test_LT-5-01', 'test_LT-5-02', 'test_LT-5-03', 'test_LT-5-04', 'test_LT-5-05'],
                    ['test_LT-4-01', 'test_LT-4-02', 'test_LT-4-03', 'test_LT-4-04', 'test_LT-4-05']];


testing_dataset = ['train_2-1-01', 'train_2-1-02', 'train_2-1-03', 'train_2-1-04', 'train_2-1-05',
                   'train_2-2-01', 'train_2-2-02', 'train_2-2-03', 'train_2-2-04', 'train_2-2-05',
                   'train_spaceBar-01', 'train_spaceBar-02', 'train_spaceBar-03', 'train_spaceBar-04', 'train_spaceBar-05',
                   'train_canteen-01', 'train_canteen-02', 'train_canteen-03', 'train_canteen-04', 'train_canteen-05', 'train_canteen-06',
                   'train_LT-5-01', 'train_LT-5-02', 'train_LT-5-03', 'train_LT-5-04', 'train_LT-5-05',
                   'train_LT-4-01', 'train_LT-4-02', 'train_LT-4-03', 'train_LT-4-04', 'train_LT-4-05']

#get_all_room_fp(training_dataset)
#generate_room_fp(testing_dataset,getAllSsid(training_dataset))
a = getAllSsid(training_dataset)
print a
#getCurrentLocation(testing_dataset,getAllSsid(training_dataset))               


