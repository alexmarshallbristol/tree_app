import requests
from bs4 import BeautifulSoup
import re 
import json
import geopy.distance
import pandas as pd
import time

verbose = True

def get_attributes(url, tree_id, verbose = False):

    response = requests.get(url)

    if response.status_code == 500: return "BROKEN LINK"

    soup = BeautifulSoup(response.content, "html.parser")

    for i in soup.find_all("script"):
        if "var tree" in i.get_text():
            text = i.get_text()

            x = re.split(r'{|}', text)
            x = '{'+' '.join(x[1:-1])+'}'

            x = x.replace('": "', '": "None","').replace('"recorder": "name"', '"recorder": "None", "name"').replace('[','{').replace(']','}').replace('"species": "name"', '"species": "None","species_name"').replace('"species": "None","name"', '"species": "None","species_name"').replace('"veteranStatus": "name":','"veteranStatusBLANK": "BLANK","veteranStatus":').replace('"veteranStatus": "None","name":', '"veteranStatus": "None","veteranStatus":').replace('"publicAccessibilityStatus": "None","name":', '"publicAccessibilityStatusBLANK": "None","publicAccessibilityStatus":')

            dictionary = json.loads(x)

            dictionary["TNSI"] = "False"
            if "Trees of National Special Interest (TNSI)" in x:
                dictionary["TNSI"] = "True"
            dictionary["heritageTree"] = "False"
            if "Heritage Tree" in x:
                dictionary["heritageTree"] = "True"
            dictionary["TotY"] = "False"
            if "Tree of the Year – Shortlisted" in x:
                dictionary["TotY"] = "Shortlisted"
            if "Tree of the Year – Winner" in x:
                dictionary["TotY"] = "Winner"
            dictionary["championTree"] = "False"
            if "Champion Tree" in x:
                dictionary["championTree"] = "True"

            if verbose:
                for key in dictionary:
                    print(key,':    ', dictionary[key],'\n')
            
            attributes_to_keep = ["latitude", "longitude", "species_name", 
                                    "localName", "veteranStatus", "publicAccessibilityStatus",
                                    "TNSI", "heritageTree", "TotY", "championTree"]

            attributes = {}
            for attribute in attributes_to_keep:
                attributes[attribute] = dictionary[attribute]

            # attributes['url'] = url
            attributes['url'] = tree_id

            return attributes


start_time = time.time()
N_samples = 245000
print("Scraping...")
for tree_id in range(1, N_samples):
    try:
        attributes = get_attributes(f"https://ati.woodlandtrust.org.uk/tree-search/tree?treeid={tree_id}#/", tree_id)
        if verbose:
            print('\n')
            for attribute in attributes:
                print(attribute,':  ',attributes[attribute])
        try:
            df_i = pd.DataFrame(attributes, index=[tree_id])
            df = pd.concat([df, df_i])
        except:
            df = pd.DataFrame(attributes, index=[tree_id])
    except: 
        if verbose:
            print(f"Broken {tree_id}")
        pass

    if tree_id % 25 == 0 and tree_id > 0:
        current_time = time.time()
        elapsed_time = current_time-start_time
        try:
            print(f"{tree_id}, {df.shape}, elapsed_time: {elapsed_time}, time per sample: {elapsed_time/tree_id}, expected time remaining: {(N_samples-tree_id)*(elapsed_time/tree_id)}")
        except:
            pass

print(df)

df.to_csv('trees.csv',header=False, index=False)








