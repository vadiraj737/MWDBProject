function visualize(file, inputMatrix)
XMAT=csvread(file);
XNorm=2*mat2gray(XMAT)-1;
colormap('gray');
imagesc(XNorm);
arr=csvread(inputMatrix);
for i=1:10
x=arr(i,1);
y=arr(i,2);
h=imrect(gca, [x y 3 1]);
setFixedAspectRatioMode(h,true);
setResizable(h,false);
end
print(gcf,'-dpng','1.png');
end