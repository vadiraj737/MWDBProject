function quadratic_mwdb(file)
h= csvread(file)
[rows,cols]= size(h)
f= ones(1,rows)
x= quadprog(h,f)
csvwrite('hmatrix.csv',x)
end