import pandas as pd
import numpy as np
import re
import pdb

"""
0. Scanned file should have a combination of Full Name, (full) Street Address
1. check name
2. check combination of Street_Number, Street_Name, Street_Type
       A. check Street_Dir_Suffix if the street is NTH, ALPHABET LETTER, Penn. Ave, Mass. Ave, Independence Ave, or Capitol St
"""

CRITICAL_COLS = ['Last_Name', 'First_Name', 'Street_Number',
       'Street_Name', 'Street_Type', 'Street_Dir_Suffix', 'Unit_Type',
       'Apartment_Number', 'WARD']

def get_matches(scanned_filename: str, registry_filename: str) -> pd.DataFrame:
    FULL_NAME = 'full_name'
    FULL_ADDRESS_NO_APT = 'full_address_no_apt'
    voter_registry = pd.read_csv(registry_filename, usecols=CRITICAL_COLS[:6])
    ocr_df = pd.read_csv(scanned_filename) #, usecols=CRITICAL_COLS[:5])
    ocr_df = ocr_df.rename(columns={'Street_Cardinal_Direction': 'Street_Dir_Suffix'})
    # pdb.set_trace()
    
    def combine_names(row):
        return f"{row['First_Name']} {row['Last_Name']}"
    
    def combine_address(row, type_included: bool) -> str:
        corrected_num = "" if np.isnan(row['Street_Number']) else int(row['Street_Number'])
        house_num = f"{corrected_num} {row['Street_Name']}"
        if not(type_included):
            house_num += row['Street_Type']
        corrected_suffix = "" if str(row['Street_Dir_Suffix']) == 'nan' else row['Street_Dir_Suffix']
        return re.sub("[^a-zA-Z0-9\s]", "", f"{house_num} {corrected_suffix}".strip())
    
    ocr_df[FULL_NAME] = ocr_df.apply(combine_names, axis=1)
    ocr_df[FULL_NAME] = ocr_df[FULL_NAME].map(lambda s: s.upper()).astype(str)
    ocr_df[FULL_ADDRESS_NO_APT] = ocr_df.apply(combine_address, axis=1, type_included=True)
    ocr_df[FULL_ADDRESS_NO_APT] = ocr_df[FULL_ADDRESS_NO_APT].map(lambda s: s.upper()).astype(str)
    pdb.set_trace()
    
    voter_registry[FULL_NAME] = voter_registry.apply(combine_names, axis=1) # voter_registry.apply(lambda row: f"{row.First_Name} {row.Last_Name}".upper(), axis = 1)
    voter_registry[FULL_NAME] = voter_registry[FULL_NAME].astype(str)
    voter_registry[FULL_ADDRESS_NO_APT] = voter_registry.apply(combine_address, axis=1, type_included=True)
    voter_registry[FULL_ADDRESS_NO_APT] = voter_registry[FULL_ADDRESS_NO_APT].map(lambda s: s.upper()).astype(str)
    
    ocr_df.set_index(FULL_NAME)
    voter_registry.set_index(FULL_NAME)
    
    df_fullname_match = ocr_df.merge(voter_registry, on=FULL_NAME, how='inner')
    df_fullname_match[FULL_NAME].value_counts()
    
    name_counts = df_fullname_match[FULL_NAME].value_counts()
    no_dups = set(name_counts[name_counts == 1].keys())
    df_no_dups = df_fullname_match[df_fullname_match[FULL_NAME].isin(no_dups)]
    
    dups = set(name_counts[name_counts > 1].keys())
    ocr_dups = ocr_df[ocr_df[FULL_NAME].isin(dups)]
    df_dups = ocr_dups.merge(voter_registry, on=[FULL_NAME, FULL_ADDRESS_NO_APT], how='inner')
    
    deduped = []
    return df_fullname_match
#       non_matches = df_fullname_match[df_fullname_match['Last_Name_x'].isna()]

"""
df_fullname_match[df_fullname_match['Street_Dir_Suffix'].isnull()]
about 30 non-matches by full name in OCR_DF
e.g.
Eric B. Johnson (row index 87), 4800 Ga. Ave NW #302

voter_registry[(voter_registry['Last_Name'] == 'Johnson') & (voter_registry['First_Name'] == 'Eric')]
"""

"""
voter_registry.iloc[419] Street_Name = 'Georgia', Street_Type = 'Ave', etc.
"""

print(get_matches("../Aggregated Data/output.csv",
                  "./voter_registries/raw_feb_23_city_wide.csv"))

"""
df = pd.read_csv("raw_feb_23_city_wide.csv", usecols=['Last_Name', 'First_Name', 'Street_Number', 'Street_Name'])
deck_indices = df.index[df['Last_Name'] == 'Deck'].tolist()
df.iloc[deck_indices]

voter_registry.loc[(voter_registry['Street_Number'] == 620) & (voter_registry['Street_Name'] == 'Michigan') & (voter_registry['Last_Name'] == 'Morgan')]
df_fullname_match.loc[(df_fullname_match['Last_Name_x'] == 'Morgan') & (df_fullname_match['First_Name_x'] == 'Andrew')]
"""